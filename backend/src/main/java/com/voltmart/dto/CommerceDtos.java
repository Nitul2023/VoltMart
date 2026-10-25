package com.voltmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class CommerceDtos {
    private CommerceDtos() {}

    public record CartItemRequest(@NotNull UUID variantId, @Min(1) @Max(99) int quantity) {}

    public record CartItemView(UUID id, UUID variantId, UUID productId, String productName, String variantName,
                               String sku, String imageUrl, BigDecimal unitPrice, int quantity,
                               int availableQty, BigDecimal lineTotal) {}

    public record CartView(UUID id, List<CartItemView> items, BigDecimal subtotal, int itemCount) {}

    public record AddressRequest(
            String label,
            @NotBlank String line1,
            String line2,
            @NotBlank String city,
            @NotBlank String state,
            @NotBlank String postalCode,
            String country,
            boolean defaultAddress
    ) {}

    public record AddressView(UUID id, String label, String line1, String line2, String city,
                              String state, String postalCode, String country, boolean defaultAddress) {}

    public record CheckoutRequest(
            @Valid @NotNull AddressRequest shipping,
            @NotBlank String shippingMethod,
            String couponCode,
            @NotBlank String paymentMethod,
            @Pattern(regexp = "\\d{13,19}") String cardNumber,
            @Pattern(regexp = "\\d{2}/\\d{2}") String cardExpiry,
            @Pattern(regexp = "\\d{3,4}") String cardCvc,
            String notes
    ) {}

    public record OrderItemView(UUID id, UUID productId, UUID variantId, String sku, String productName,
                                String variantName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}

    public record PaymentView(UUID id, String provider, String method, String status, BigDecimal amount,
                              String transactionRef, String last4, String brand, boolean verified) {}

    public record ShipmentView(UUID id, String carrier, String trackingNumber, String status,
                               Instant shippedAt, Instant deliveredAt) {}

    public record InvoiceView(UUID id, String invoiceNumber, Instant issuedAt, String html) {}

    public record OrderView(UUID id, String orderNumber, String status, BigDecimal subtotal, BigDecimal discount,
                            BigDecimal shippingCost, BigDecimal tax, BigDecimal total, String couponCode,
                            String shippingName, String shippingLine1, String shippingLine2, String shippingCity,
                            String shippingState, String shippingPostal, String shippingCountry,
                            String shippingMethod, Instant createdAt, List<OrderItemView> items,
                            PaymentView payment, ShipmentView shipment, InvoiceView invoice) {}

    public record ReturnRequestDto(@NotBlank String reason) {}

    public record ReturnView(UUID id, UUID orderId, String orderNumber, String reason, String status,
                             BigDecimal refundAmount, Instant createdAt) {}

    public record ReviewRequest(@Min(1) @Max(5) int rating, String title, @NotBlank String body) {}

    public record CouponPreview(String code, String description, BigDecimal discount, boolean valid, String message) {}
}
