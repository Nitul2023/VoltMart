package com.voltmart.service;

import com.voltmart.domain.entity.*;
import com.voltmart.domain.enums.OrderStatus;
import com.voltmart.domain.enums.ReturnStatus;
import com.voltmart.domain.enums.ShipmentStatus;
import com.voltmart.dto.AdminDtos.*;
import com.voltmart.dto.CatalogDtos.ProductDetail;
import com.voltmart.dto.CommerceDtos.OrderView;
import com.voltmart.exception.ApiException;
import com.voltmart.mapper.ProductMapper;
import com.voltmart.repository.*;
import com.voltmart.util.Slugs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductVariantRepository variantRepository;
    private final BannerRepository bannerRepository;
    private final CmsPageRepository cmsPageRepository;
    private final CouponRepository couponRepository;
    private final PromotionRepository promotionRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final RefundRepository refundRepository;
    private final ShipmentRepository shipmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final ReviewRepository reviewRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final PaymentGateway paymentGateway;
    private final OrderService orderService;

    public AdminService(ProductRepository productRepository, CategoryRepository categoryRepository,
                        BrandRepository brandRepository, ProductVariantRepository variantRepository,
                        BannerRepository bannerRepository, CmsPageRepository cmsPageRepository,
                        CouponRepository couponRepository, PromotionRepository promotionRepository,
                        OrderRepository orderRepository, UserRepository userRepository,
                        ReturnRequestRepository returnRequestRepository, RefundRepository refundRepository,
                        ShipmentRepository shipmentRepository, AuditLogRepository auditLogRepository,
                        ReviewRepository reviewRepository, AuditService auditService,
                        NotificationService notificationService, PaymentGateway paymentGateway,
                        OrderService orderService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.variantRepository = variantRepository;
        this.bannerRepository = bannerRepository;
        this.cmsPageRepository = cmsPageRepository;
        this.couponRepository = couponRepository;
        this.promotionRepository = promotionRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.refundRepository = refundRepository;
        this.shipmentRepository = shipmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.reviewRepository = reviewRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.paymentGateway = paymentGateway;
        this.orderService = orderService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EDITOR')")
    @Transactional(readOnly = true)
    public Page<ProductDetail> productDetails(int page, int size) {
        return productRepository.findAllByActiveTrue(PageRequest.of(page, size, Sort.by("updatedAt").descending()))
                .map(p -> ProductMapper.toDetail(p, reviewRepository.averageRating(p.getId()),
                        reviewRepository.countByProductId(p.getId())));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EDITOR')")
    @Transactional
    public ProductDetail saveProduct(UUID id, ProductWrite write) {
        Product product = id == null ? new Product() : productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product not found"));
        String sku = write.sku().trim();
        boolean duplicateSku = id == null
                ? productRepository.existsBySku(sku)
                : productRepository.existsBySkuAndIdNot(sku, id);
        if (duplicateSku) {
            throw ApiException.conflict("A product with SKU '" + sku + "' already exists. Use a unique SKU.");
        }
        product.setSku(sku);
        product.setName(write.name().trim());
        String requestedSlug = write.slug() == null || write.slug().isBlank()
                ? Slugs.from(write.name()) + "-" + Slugs.from(sku)
                : Slugs.from(write.slug());
        if (requestedSlug.length() > 220) {
            requestedSlug = requestedSlug.substring(0, 220);
        }
        boolean duplicateSlug = id == null
                ? productRepository.existsBySlug(requestedSlug)
                : productRepository.existsBySlugAndIdNot(requestedSlug, id);
        if (duplicateSlug && write.slug() != null && !write.slug().isBlank()) {
            throw ApiException.conflict("That product slug is already in use. Leave the slug blank to generate one automatically.");
        }
        if (duplicateSlug) {
            String baseSlug = requestedSlug;
            int suffix = 2;
            while (productRepository.existsBySlug(requestedSlug)) {
                requestedSlug = baseSlug + "-" + suffix++;
            }
        }
        product.setSlug(requestedSlug);
        product.setShortDescription(write.shortDescription());
        product.setDescription(write.description());
        product.setCategory(categoryRepository.findById(write.categoryId())
                .orElseThrow(() -> ApiException.badRequest("Category not found")));
        product.setBrand(brandRepository.findById(write.brandId())
                .orElseThrow(() -> ApiException.badRequest("Brand not found")));
        product.setBasePrice(write.basePrice());
        product.setCompareAtPrice(write.compareAtPrice());
        product.setDiscountPercent(write.discountPercent());
        product.setWarrantyMonths(write.warrantyMonths() == null ? 12 : write.warrantyMonths());
        product.setWeightGrams(write.weightGrams());
        product.setActive(write.active() == null || write.active());
        product.setFeatured(Boolean.TRUE.equals(write.featured()));
        product.setSeoTitle(write.seoTitle());
        product.setSeoDescription(write.seoDescription());

        product.getImages().clear();
        if (write.images() != null) {
            int i = 0;
            for (ImageWrite img : write.images()) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setUrl(img.url());
                image.setAltText(img.altText());
                image.setSortOrder(img.sortOrder() == null ? i : img.sortOrder());
                product.getImages().add(image);
                i++;
            }
        }
        product.getSpecs().clear();
        if (write.specs() != null) {
            int i = 0;
            for (SpecWrite spec : write.specs()) {
                ProductSpec s = new ProductSpec();
                s.setProduct(product);
                s.setGroupName(spec.groupName());
                s.setSpecKey(spec.specKey());
                s.setSpecValue(spec.specValue());
                s.setSortOrder(spec.sortOrder() == null ? i : spec.sortOrder());
                product.getSpecs().add(s);
                i++;
            }
        }
        if (write.variants() != null) {
            for (VariantWrite vw : write.variants()) {
                ProductVariant variant = vw.id() != null
                        ? variantRepository.findById(vw.id()).orElse(new ProductVariant())
                        : new ProductVariant();
                if (vw.sku() == null || vw.sku().isBlank() || vw.name() == null || vw.name().isBlank() || vw.price() == null) {
                    throw ApiException.badRequest("Each product variant requires a SKU, name, and price.");
                }
                if (vw.sku().length() > 64) {
                    throw ApiException.badRequest("Variant SKU must be 64 characters or fewer.");
                }
                boolean duplicateVariantSku = vw.id() == null
                        ? variantRepository.existsBySku(vw.sku().trim())
                        : variantRepository.existsBySkuAndIdNot(vw.sku().trim(), vw.id());
                if (duplicateVariantSku) {
                    throw ApiException.conflict("Variant SKU '" + vw.sku().trim() + "' already exists. Use a unique variant SKU.");
                }
                if (vw.stockQty() != null && vw.stockQty() < 0) {
                    throw ApiException.badRequest("Variant stock cannot be negative.");
                }
                variant.setProduct(product);
                variant.setSku(vw.sku().trim());
                variant.setName(vw.name().trim());
                variant.setColor(vw.color());
                variant.setStorage(vw.storage());
                variant.setOtherOption(vw.otherOption());
                variant.setPrice(vw.price());
                variant.setCompareAtPrice(vw.compareAtPrice());
                if (vw.stockQty() != null) variant.setStockQty(vw.stockQty());
                variant.setActive(vw.active() == null || vw.active());
                if (!product.getVariants().contains(variant) && variant.getId() == null) {
                    product.getVariants().add(variant);
                } else if (variant.getId() != null) {
                    variantRepository.save(variant);
                }
            }
        }
        product = productRepository.save(product);
        auditService.log(id == null ? "CREATE" : "UPDATE", "PRODUCT", product.getId().toString(), product.getSku());
        Double avg = reviewRepository.averageRating(product.getId());
        long count = reviewRepository.countByProductId(product.getId());
        return ProductMapper.toDetail(product, avg, count);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product not found"));
        if (!product.isActive()) {
            throw ApiException.notFound("Product not found");
        }
        product.setActive(false);
        productRepository.save(product);
        auditService.log("DELETE", "PRODUCT", product.getId().toString(), product.getSku());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Transactional
    public void adjustInventory(InventoryAdjust adjust) {
        ProductVariant variant = variantRepository.findByIdForUpdate(adjust.variantId())
                .orElseThrow(() -> ApiException.notFound("Variant not found"));
        if (adjust.stockQty() < 0) {
            throw ApiException.badRequest("Stock cannot be negative");
        }
        variant.setStockQty(adjust.stockQty());
        variantRepository.save(variant);
        auditService.log("INVENTORY", "VARIANT", variant.getId().toString(), "qty=" + adjust.stockQty());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EDITOR')")
    @Transactional
    public Category saveCategory(Long id, CategoryWrite write) {
        Category category = id == null ? new Category() : categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Category not found"));
        category.setName(write.name());
        category.setSlug(write.slug() == null || write.slug().isBlank() ? Slugs.from(write.name()) : write.slug());
        category.setDescription(write.description());
        category.setImageUrl(write.imageUrl());
        category.setSortOrder(write.sortOrder() == null ? 0 : write.sortOrder());
        category.setActive(write.active() == null || write.active());
        category.setSeoTitle(write.seoTitle());
        category.setSeoDescription(write.seoDescription());
        if (write.parentId() != null) {
            category.setParent(categoryRepository.findById(write.parentId())
                    .orElseThrow(() -> ApiException.badRequest("Parent not found")));
        } else {
            category.setParent(null);
        }
        category = categoryRepository.save(category);
        auditService.log(id == null ? "CREATE" : "UPDATE", "CATEGORY", category.getId().toString(), category.getSlug());
        return category;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EDITOR')")
    @Transactional
    public Brand saveBrand(Long id, BrandWrite write) {
        Brand brand = id == null ? new Brand() : brandRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Brand not found"));
        brand.setName(write.name());
        brand.setSlug(write.slug() == null || write.slug().isBlank() ? Slugs.from(write.name()) : write.slug());
        brand.setLogoUrl(write.logoUrl());
        brand.setDescription(write.description());
        brand.setActive(write.active() == null || write.active());
        brand = brandRepository.save(brand);
        auditService.log(id == null ? "CREATE" : "UPDATE", "BRAND", brand.getId().toString(), brand.getSlug());
        return brand;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EDITOR')")
    @Transactional
    public Banner saveBanner(Long id, BannerWrite write) {
        Banner banner = id == null ? new Banner() : bannerRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Banner not found"));
        banner.setTitle(write.title());
        banner.setSubtitle(write.subtitle());
        banner.setImageUrl(write.imageUrl());
        banner.setCtaLabel(write.ctaLabel());
        banner.setCtaUrl(write.ctaUrl());
        banner.setPlacement(write.placement());
        banner.setSortOrder(write.sortOrder() == null ? 0 : write.sortOrder());
        banner.setActive(write.active() == null || write.active());
        banner = bannerRepository.save(banner);
        auditService.log(id == null ? "CREATE" : "UPDATE", "BANNER", banner.getId().toString(), banner.getTitle());
        return banner;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EDITOR')")
    @Transactional
    public CmsPage savePage(Long id, PageWrite write) {
        CmsPage page = id == null ? new CmsPage() : cmsPageRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Page not found"));
        page.setSlug(write.slug());
        page.setTitle(write.title());
        page.setBody(write.body());
        page.setSeoTitle(write.seoTitle());
        page.setSeoDescription(write.seoDescription());
        page.setPublished(write.published() == null || write.published());
        page = cmsPageRepository.save(page);
        auditService.log(id == null ? "CREATE" : "UPDATE", "PAGE", page.getId().toString(), page.getSlug());
        return page;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Transactional
    public Coupon saveCoupon(Long id, CouponWrite write) {
        Coupon coupon = id == null ? new Coupon() : couponRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Coupon not found"));
        coupon.setCode(write.code().toUpperCase());
        coupon.setDescription(write.description());
        coupon.setDiscountType(write.discountType());
        coupon.setAmount(write.amount());
        coupon.setMinOrder(write.minOrder() == null ? BigDecimal.ZERO : write.minOrder());
        coupon.setMaxUses(write.maxUses());
        coupon.setActive(write.active() == null || write.active());
        coupon = couponRepository.save(coupon);
        auditService.log(id == null ? "CREATE" : "UPDATE", "COUPON", coupon.getId().toString(), coupon.getCode());
        return coupon;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EDITOR')")
    @Transactional
    public Promotion savePromotion(Long id, PromotionWrite write) {
        Promotion promo = id == null ? new Promotion() : promotionRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Promotion not found"));
        promo.setName(write.name());
        promo.setDescription(write.description());
        promo.setPromoType(write.promoType());
        promo.setValue(write.value());
        promo.setProductId(write.productId());
        promo.setCategoryId(write.categoryId());
        promo.setActive(write.active() == null || write.active());
        promo = promotionRepository.save(promo);
        auditService.log(id == null ? "CREATE" : "UPDATE", "PROMOTION", promo.getId().toString(), promo.getName());
        return promo;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPPORT')")
    @Transactional(readOnly = true)
    public Page<OrderView> orderViews(int page, int size, OrderStatus status) {
        Page<Order> orders = status == null
                ? orderRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                : orderRepository.findByStatus(status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return orders.map(orderService::toView);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPPORT')")
    @Transactional
    public OrderView updateOrder(UUID orderId, OrderStatusWrite write) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> ApiException.notFound("Order not found"));
        OrderStatus next = OrderStatus.valueOf(write.status());
        order.setStatus(next);
        if (next == OrderStatus.SHIPPED) {
            Shipment shipment = shipmentRepository.findByOrderId(order.getId()).stream().findFirst()
                    .orElseGet(Shipment::new);
            shipment.setOrder(order);
            shipment.setCarrier(write.carrier() == null ? "VoltMart Logistics" : write.carrier());
            shipment.setTrackingNumber(write.trackingNumber() == null
                    ? "VMTRACK" + order.getOrderNumber().replace("-", "")
                    : write.trackingNumber());
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            shipment.setShippedAt(Instant.now());
            shipmentRepository.save(shipment);
            notificationService.send(order.getUser().getEmail(), "Order " + order.getOrderNumber() + " shipped",
                    "Tracking: " + shipment.getTrackingNumber(), "ORDER", order.getId().toString());
        }
        if (next == OrderStatus.DELIVERED) {
            shipmentRepository.findByOrderId(order.getId()).forEach(s -> {
                s.setStatus(ShipmentStatus.DELIVERED);
                s.setDeliveredAt(Instant.now());
                shipmentRepository.save(s);
            });
        }
        orderRepository.save(order);
        auditService.log("STATUS", "ORDER", order.getId().toString(), next.name());
        return orderService.toView(order);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPPORT')")
    @Transactional
    public void resolveReturn(UUID returnId, boolean approve) {
        ReturnRequest rr = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> ApiException.notFound("Return not found"));
        if (approve) {
            rr.setStatus(ReturnStatus.REFUNDED);
            rr.setResolvedAt(Instant.now());
            Refund refund = new Refund();
            refund.setReturnRequest(rr);
            refund.setOrder(rr.getOrder());
            refund.setAmount(rr.getRefundAmount() == null ? rr.getOrder().getTotal() : rr.getRefundAmount());
            refund.setStatus("COMPLETED");
            refund.setTransactionRef(paymentGateway.refund("return", refund.getAmount()));
            refundRepository.save(refund);
            rr.getOrder().setStatus(OrderStatus.REFUNDED);
            orderRepository.save(rr.getOrder());
            notificationService.send(rr.getUser().getEmail(), "Refund issued",
                    "Your return was approved and refunded.", "RETURN", rr.getId().toString());
        } else {
            rr.setStatus(ReturnStatus.REJECTED);
            rr.setResolvedAt(Instant.now());
            rr.getOrder().setStatus(OrderStatus.DELIVERED);
            orderRepository.save(rr.getOrder());
        }
        returnRequestRepository.save(rr);
        auditService.log(approve ? "REFUND" : "REJECT", "RETURN", rr.getId().toString(), null);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPPORT')")
    @Transactional(readOnly = true)
    public List<CustomerView> customers() {
        return userRepository.findAll().stream()
                .map(u -> new CustomerView(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                        u.isEnabled(), u.getRoles().stream().map(Role::getName).toList(), u.getCreatedAt()))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void setCustomerEnabled(UUID userId, boolean enabled) {
        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
        auditService.log(enabled ? "ENABLE" : "DISABLE", "USER", userId.toString(), user.getEmail());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Transactional(readOnly = true)
    public ReportSummary report() {
        Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long lowStock = variantRepository.findAll().stream().filter(v -> v.availableQty() < 8).count();
        long openReturns = returnRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReturnStatus.REQUESTED || r.getStatus() == ReturnStatus.APPROVED)
                .count();
        return new ReportSummary(
                orderRepository.countByCreatedAtAfter(start),
                orderRepository.revenueSince(start),
                userRepository.count(),
                lowStock,
                openReturns
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Transactional(readOnly = true)
    public Page<AuditLog> audits(int page, int size) {
        return auditLogRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public List<Banner> allBanners() { return bannerRepository.findAll(Sort.by("sortOrder")); }
    public List<CmsPage> allPages() { return cmsPageRepository.findAll(Sort.by("slug")); }
    public List<Coupon> allCoupons() { return couponRepository.findAll(); }
    public List<Promotion> allPromotions() { return promotionRepository.findAll(); }
    public List<com.voltmart.dto.CommerceDtos.ReturnView> returnViews() {
        return returnRequestRepository.findAll().stream()
                .map(rr -> new com.voltmart.dto.CommerceDtos.ReturnView(
                        rr.getId(), rr.getOrder().getId(), rr.getOrder().getOrderNumber(),
                        rr.getReason(), rr.getStatus().name(), rr.getRefundAmount(), rr.getCreatedAt()))
                .toList();
    }
    public List<Brand> allBrands() { return brandRepository.findAll(); }
    public List<Category> allCategories() { return categoryRepository.findAll(); }
}
