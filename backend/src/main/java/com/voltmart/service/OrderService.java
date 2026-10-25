package com.voltmart.service;

import com.voltmart.domain.entity.*;
import com.voltmart.domain.enums.OrderStatus;
import com.voltmart.domain.enums.PaymentStatus;
import com.voltmart.domain.enums.ReturnStatus;
import com.voltmart.domain.enums.ShipmentStatus;
import com.voltmart.dto.CommerceDtos.*;
import com.voltmart.exception.ApiException;
import com.voltmart.repository.*;
import com.voltmart.util.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private final CartService cartService;
    private final CouponService couponService;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final RefundRepository refundRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BigDecimal taxRate;
    private final BigDecimal freeShippingThreshold;

    public OrderService(CartService cartService, CouponService couponService, PaymentGateway paymentGateway,
                        NotificationService notificationService, AuditService auditService,
                        OrderRepository orderRepository, ProductVariantRepository variantRepository,
                        PaymentRepository paymentRepository, ShipmentRepository shipmentRepository,
                        InvoiceRepository invoiceRepository, ReturnRequestRepository returnRequestRepository,
                        RefundRepository refundRepository, UserRepository userRepository,
                        AddressRepository addressRepository,
                        @Value("${app.store.tax-rate}") BigDecimal taxRate,
                        @Value("${app.store.free-shipping-threshold}") BigDecimal freeShippingThreshold) {
        this.cartService = cartService;
        this.couponService = couponService;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.orderRepository = orderRepository;
        this.variantRepository = variantRepository;
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.refundRepository = refundRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.taxRate = taxRate;
        this.freeShippingThreshold = freeShippingThreshold;
    }

    @Transactional
    public OrderView checkout(CheckoutRequest request) {
        UUID userId = CurrentUser.requireId();
        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.unauthorized("User not found"));
        Cart cart = cartService.requireUserCart();
        if (cart.getItems().isEmpty()) {
            throw ApiException.badRequest("Cart is empty");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            ProductVariant locked = variantRepository.findByIdForUpdate(item.getVariant().getId())
                    .orElseThrow(() -> ApiException.notFound("Variant missing"));
            if (item.getQuantity() > locked.availableQty()) {
                throw ApiException.badRequest("Insufficient stock for " + locked.getSku());
            }
            locked.setReservedQty(locked.getReservedQty() + item.getQuantity());
            variantRepository.save(locked);
            subtotal = subtotal.add(locked.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            coupon = couponService.requireValid(request.couponCode(), subtotal);
            discount = couponService.calculate(coupon, subtotal);
        }

        BigDecimal shipping = shippingCost(request.shippingMethod(), subtotal.subtract(discount));
        BigDecimal taxable = subtotal.subtract(discount).add(shipping);
        BigDecimal tax = taxable.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = taxable.add(tax).setScale(2, RoundingMode.HALF_UP);

        Order order = new Order();
        order.setOrderNumber(nextOrderNumber());
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingCost(shipping);
        order.setTax(tax);
        order.setTotal(total);
        order.setCouponCode(coupon != null ? coupon.getCode() : null);
        order.setShippingName(user.getFirstName() + " " + user.getLastName());
        order.setShippingLine1(request.shipping().line1());
        order.setShippingLine2(request.shipping().line2());
        order.setShippingCity(request.shipping().city());
        order.setShippingState(request.shipping().state());
        order.setShippingPostal(request.shipping().postalCode());
        order.setShippingCountry(request.shipping().country() == null ? "United States" : request.shipping().country());
        order.setShippingMethod(request.shippingMethod());
        order.setNotes(request.notes());

        for (CartItem item : cart.getItems()) {
            ProductVariant v = item.getVariant();
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(v.getProduct().getId());
            oi.setVariantId(v.getId());
            oi.setSku(v.getSku());
            oi.setProductName(v.getProduct().getName());
            oi.setVariantName(v.getName());
            oi.setUnitPrice(v.getPrice());
            oi.setQuantity(item.getQuantity());
            oi.setLineTotal(v.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            order.getItems().add(oi);
        }
        order = orderRepository.save(order);

        PaymentGateway.ChargeResult charge = paymentGateway.charge(
                request.cardNumber(), request.cardExpiry(), request.cardCvc(), total);
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setProvider("VOLTMART_PAY");
        payment.setMethod(request.paymentMethod());
        payment.setAmount(total);
        payment.setTransactionRef(charge.transactionRef());
        payment.setLast4(charge.last4());
        payment.setBrand(charge.brand());
        payment.setVerified(charge.verified());
        if (!charge.success()) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(charge.failureReason());
            paymentRepository.save(payment);
            releaseStock(order);
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(Instant.now());
            order.setCancelReason("Payment failed: " + charge.failureReason());
            orderRepository.save(order);
            throw ApiException.badRequest("Payment failed: " + charge.failureReason());
        }
        payment.setStatus(PaymentStatus.CAPTURED);
        paymentRepository.save(payment);

        commitStock(order);
        if (coupon != null) {
            couponService.increment(coupon);
        }
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceNumber("INV-" + order.getOrderNumber());
        invoiceRepository.save(invoice);

        cart.getItems().clear();
        notificationService.send(user.getEmail(), "Order " + order.getOrderNumber() + " confirmed",
                "Thanks for shopping VoltMart. Your payment was verified and we are preparing your order.",
                "ORDER", order.getId().toString());
        auditService.log("CHECKOUT", "ORDER", order.getId().toString(), "Paid " + total);
        saveAddress(user, request.shipping());
        return toView(order);
    }

    @Transactional(readOnly = true)
    public PageResultWrapper listMine(int page, int size) {
        UUID userId = CurrentUser.requireId();
        Page<Order> result = orderRepository.findByUserId(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return new PageResultWrapper(result.getContent().stream().map(this::toView).toList(),
                result.getTotalElements(), result.getNumber(), result.getSize(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public OrderView getMine(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!order.getUser().getId().equals(CurrentUser.requireId())) {
            throw ApiException.forbidden("Not your order");
        }
        return toView(order);
    }

    @Transactional
    public OrderView cancel(String orderNumber, String reason) {
        Order order = owned(orderNumber);
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PROCESSING
                && order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw ApiException.badRequest("Order cannot be cancelled in status " + order.getStatus());
        }
        restock(order);
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancelReason(reason);
        orderRepository.save(order);
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setAmount(order.getTotal());
        refund.setStatus("COMPLETED");
        refund.setTransactionRef(paymentGateway.refund("cancel", order.getTotal()));
        refundRepository.save(refund);
        notificationService.send(order.getUser().getEmail(), "Order " + order.getOrderNumber() + " cancelled",
                "Your order was cancelled and a refund was issued.", "ORDER", order.getId().toString());
        return toView(order);
    }

    @Transactional
    public ReturnView requestReturn(String orderNumber, ReturnRequestDto dto) {
        Order order = owned(orderNumber);
        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.SHIPPED) {
            throw ApiException.badRequest("Returns are available after shipment");
        }
        ReturnRequest rr = new ReturnRequest();
        rr.setOrder(order);
        rr.setUser(order.getUser());
        rr.setReason(dto.reason());
        rr.setStatus(ReturnStatus.REQUESTED);
        rr.setRefundAmount(order.getTotal());
        rr = returnRequestRepository.save(rr);
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);
        notificationService.send(order.getUser().getEmail(), "Return requested for " + order.getOrderNumber(),
                "We received your return request.", "RETURN", rr.getId().toString());
        return toReturnView(rr);
    }

    @Transactional(readOnly = true)
    public InvoiceView invoice(String orderNumber) {
        Order order = owned(orderNumber);
        Invoice invoice = invoiceRepository.findByOrderId(order.getId())
                .orElseThrow(() -> ApiException.notFound("Invoice not found"));
        return new InvoiceView(invoice.getId(), invoice.getInvoiceNumber(), invoice.getIssuedAt(), renderInvoice(order, invoice));
    }

    @Transactional(readOnly = true)
    public List<ShipmentView> track(String orderNumber) {
        Order order = owned(orderNumber);
        return shipmentRepository.findByOrderId(order.getId()).stream().map(this::toShipment).toList();
    }

    public record PageResultWrapper(List<OrderView> items, long total, int page, int size, int totalPages) {}

    Order owned(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!order.getUser().getId().equals(CurrentUser.requireId())) {
            throw ApiException.forbidden("Not your order");
        }
        return order;
    }

    public OrderView toView(Order order) {
        Payment payment = paymentRepository.findByOrderId(order.getId()).stream().findFirst().orElse(null);
        Shipment shipment = shipmentRepository.findByOrderId(order.getId()).stream().findFirst().orElse(null);
        Invoice invoice = invoiceRepository.findByOrderId(order.getId()).orElse(null);
        return new OrderView(
                order.getId(), order.getOrderNumber(), order.getStatus().name(), order.getSubtotal(),
                order.getDiscount(), order.getShippingCost(), order.getTax(), order.getTotal(),
                order.getCouponCode(), order.getShippingName(), order.getShippingLine1(), order.getShippingLine2(),
                order.getShippingCity(), order.getShippingState(), order.getShippingPostal(),
                order.getShippingCountry(), order.getShippingMethod(), order.getCreatedAt(),
                order.getItems().stream().map(i -> new OrderItemView(i.getId(), i.getProductId(), i.getVariantId(),
                        i.getSku(), i.getProductName(), i.getVariantName(), i.getUnitPrice(), i.getQuantity(),
                        i.getLineTotal())).toList(),
                payment == null ? null : new PaymentView(payment.getId(), payment.getProvider(), payment.getMethod(),
                        payment.getStatus().name(), payment.getAmount(), payment.getTransactionRef(),
                        payment.getLast4(), payment.getBrand(), payment.isVerified()),
                shipment == null ? null : toShipment(shipment),
                invoice == null ? null : new InvoiceView(invoice.getId(), invoice.getInvoiceNumber(),
                        invoice.getIssuedAt(), null)
        );
    }

    private ShipmentView toShipment(Shipment s) {
        return new ShipmentView(s.getId(), s.getCarrier(), s.getTrackingNumber(), s.getStatus().name(),
                s.getShippedAt(), s.getDeliveredAt());
    }

    private ReturnView toReturnView(ReturnRequest rr) {
        return new ReturnView(rr.getId(), rr.getOrder().getId(), rr.getOrder().getOrderNumber(),
                rr.getReason(), rr.getStatus().name(), rr.getRefundAmount(), rr.getCreatedAt());
    }

    private void commitStock(Order order) {
        for (OrderItem item : order.getItems()) {
            ProductVariant v = variantRepository.findByIdForUpdate(item.getVariantId())
                    .orElseThrow(() -> ApiException.notFound("Variant missing"));
            v.setReservedQty(Math.max(0, v.getReservedQty() - item.getQuantity()));
            v.setStockQty(Math.max(0, v.getStockQty() - item.getQuantity()));
            variantRepository.save(v);
        }
    }

    private void releaseStock(Order order) {
        for (OrderItem item : order.getItems()) {
            variantRepository.findByIdForUpdate(item.getVariantId()).ifPresent(v -> {
                v.setReservedQty(Math.max(0, v.getReservedQty() - item.getQuantity()));
                variantRepository.save(v);
            });
        }
    }

    private void restock(Order order) {
        for (OrderItem item : order.getItems()) {
            variantRepository.findByIdForUpdate(item.getVariantId()).ifPresent(v -> {
                v.setStockQty(v.getStockQty() + item.getQuantity());
                variantRepository.save(v);
            });
        }
    }

    private BigDecimal shippingCost(String method, BigDecimal afterDiscount) {
        if (afterDiscount.compareTo(freeShippingThreshold) >= 0 && "GROUND".equalsIgnoreCase(method)) {
            return BigDecimal.ZERO;
        }
        return switch (method == null ? "GROUND" : method.toUpperCase()) {
            case "EXPRESS" -> new BigDecimal("199.00");
            case "OVERNIGHT" -> new BigDecimal("299.00");
            default -> new BigDecimal("99.00");
        };
    }

    private String nextOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "VM-" + date + "-" + rand;
    }

    private void saveAddress(User user, AddressRequest req) {
        Address address = new Address();
        address.setUser(user);
        address.setLabel(req.label() == null ? "Shipping" : req.label());
        address.setLine1(req.line1());
        address.setLine2(req.line2());
        address.setCity(req.city());
        address.setState(req.state());
        address.setPostalCode(req.postalCode());
        address.setCountry(req.country() == null ? "United States" : req.country());
        address.setDefaultAddress(req.defaultAddress());
        addressRepository.save(address);
    }

    private String renderInvoice(Order order, Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>VoltMart Invoice ").append(invoice.getInvoiceNumber()).append("</h1>");
        sb.append("<p>Order ").append(order.getOrderNumber()).append("</p>");
        sb.append("<p>").append(order.getShippingName()).append("<br/>")
                .append(order.getShippingLine1()).append("<br/>")
                .append(order.getShippingCity()).append(", ").append(order.getShippingState()).append(" ")
                .append(order.getShippingPostal()).append("</p><table>");
        for (OrderItem item : order.getItems()) {
            sb.append("<tr><td>").append(item.getProductName()).append(" / ").append(item.getVariantName())
                    .append("</td><td>").append(item.getQuantity()).append("</td><td>$")
                    .append(item.getLineTotal()).append("</td></tr>");
        }
        sb.append("</table><p>Total $").append(order.getTotal()).append("</p>");
        return sb.toString();
    }
}
