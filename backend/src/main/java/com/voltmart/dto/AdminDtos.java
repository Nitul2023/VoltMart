package com.voltmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AdminDtos {
    private AdminDtos() {}

    public record ProductWrite(
            @NotBlank @Size(max = 60) String sku,
            @NotBlank @Size(max = 220) String name,
            @Size(max = 220) String slug,
            String shortDescription,
            String description,
            @NotNull Long categoryId,
            @NotNull Long brandId,
            @NotNull @DecimalMin(value = "0.01") BigDecimal basePrice,
            BigDecimal compareAtPrice,
            BigDecimal discountPercent,
            Integer warrantyMonths,
            Integer weightGrams,
            Boolean active,
            Boolean featured,
            String seoTitle,
            String seoDescription,
            List<ImageWrite> images,
            List<VariantWrite> variants,
            List<SpecWrite> specs
    ) {}

    public record ImageWrite(String url, String altText, Integer sortOrder) {}
    public record VariantWrite(UUID id, String sku, String name, String color, String storage,
                               String otherOption, BigDecimal price, BigDecimal compareAtPrice,
                               Integer stockQty, Boolean active) {}
    public record SpecWrite(String groupName, String specKey, String specValue, Integer sortOrder) {}

    public record CategoryWrite(@NotBlank String name, String slug, String description, String imageUrl,
                                Long parentId, Integer sortOrder, Boolean active,
                                String seoTitle, String seoDescription) {}

    public record BrandWrite(@NotBlank String name, String slug, String logoUrl, String description, Boolean active) {}

    public record BannerWrite(@NotBlank String title, String subtitle, @NotBlank String imageUrl,
                              String ctaLabel, String ctaUrl, @NotBlank String placement,
                              Integer sortOrder, Boolean active) {}

    public record PageWrite(@NotBlank String slug, @NotBlank String title, @NotBlank String body,
                            String seoTitle, String seoDescription, Boolean published) {}

    public record CouponWrite(@NotBlank String code, String description, @NotBlank String discountType,
                              @NotNull BigDecimal amount, BigDecimal minOrder, Integer maxUses, Boolean active) {}

    public record PromotionWrite(@NotBlank String name, String description, @NotBlank String promoType,
                                 BigDecimal value, UUID productId, Long categoryId, Boolean active) {}

    public record InventoryAdjust(@NotNull UUID variantId, @NotNull Integer stockQty) {}

    public record OrderStatusWrite(@NotBlank String status, String carrier, String trackingNumber) {}

    public record ReportSummary(long ordersToday, BigDecimal revenueToday, long customers,
                                long lowStockVariants, long openReturns) {}

    public record AuditView(Long id, String actorEmail, String action, String entityType,
                            String entityId, String details, Instant createdAt) {}

    public record CustomerView(UUID id, String email, String firstName, String lastName,
                               boolean enabled, List<String> roles, Instant createdAt) {}
}
