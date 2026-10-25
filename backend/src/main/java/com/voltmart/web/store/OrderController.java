package com.voltmart.web.store;

import com.voltmart.dto.CommerceDtos.*;
import com.voltmart.service.CouponService;
import com.voltmart.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;
    private final CouponService couponService;

    public OrderController(OrderService orderService, CouponService couponService) {
        this.orderService = orderService;
        this.couponService = couponService;
    }

    @PostMapping("/checkout")
    public OrderView checkout(@Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(request);
    }

    @GetMapping("/orders")
    public OrderService.PageResultWrapper myOrders(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return orderService.listMine(page, size);
    }

    @GetMapping("/orders/{orderNumber}")
    public OrderView get(@PathVariable String orderNumber) {
        return orderService.getMine(orderNumber);
    }

    @PostMapping("/orders/{orderNumber}/cancel")
    public OrderView cancel(@PathVariable String orderNumber, @RequestBody Map<String, String> body) {
        return orderService.cancel(orderNumber, body.getOrDefault("reason", "Customer cancelled"));
    }

    @PostMapping("/orders/{orderNumber}/returns")
    public ReturnView requestReturn(@PathVariable String orderNumber, @Valid @RequestBody ReturnRequestDto dto) {
        return orderService.requestReturn(orderNumber, dto);
    }

    @GetMapping("/orders/{orderNumber}/invoice")
    public InvoiceView invoice(@PathVariable String orderNumber) {
        return orderService.invoice(orderNumber);
    }

    @GetMapping("/orders/{orderNumber}/tracking")
    public List<ShipmentView> tracking(@PathVariable String orderNumber) {
        return orderService.track(orderNumber);
    }

    @GetMapping("/coupons/preview")
    public CouponPreview preview(@RequestParam String code, @RequestParam BigDecimal subtotal) {
        return couponService.preview(code, subtotal);
    }
}
