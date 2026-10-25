package com.voltmart.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class CatalogDtos {
    private CatalogDtos() {}

    public record CategoryView(Long id, String name, String slug, String description, String imageUrl,
                               Long parentId, List<CategoryView> children, String seoTitle, String seoDescription) {}

    public record BrandView(Long id, String name, String slug, String logoUrl, String description) {}

    public record ImageView(Long id, String url, String altText, int sortOrder) {}

    public record VariantView(UUID id, String sku, String name, String color, String storage,
                              String otherOption, BigDecimal price, BigDecimal compareAtPrice,
                              int stockQty, int availableQty, boolean active) {}

    public record SpecView(String groupName, String specKey, String specValue) {}

    public record FilterView(Long id, String name, String slug, String filterType, String unit) {}

    public record ProductCard(UUID id, String sku, String name, String slug, String shortDescription,
                              BigDecimal basePrice, BigDecimal compareAtPrice, BigDecimal discountPercent,
                              String imageUrl, String brand, String category, boolean featured,
                              int warrantyMonths, String availability) {}

    public record ProductDetail(UUID id, String sku, String name, String slug, String shortDescription,
                                String description, BrandView brand, CategoryView category,
                                BigDecimal basePrice, BigDecimal compareAtPrice, BigDecimal discountPercent,
                                int warrantyMonths, Integer weightGrams, boolean featured,
                                String seoTitle, String seoDescription,
                                List<ImageView> images, List<VariantView> variants, List<SpecView> specs,
                                Double averageRating, long reviewCount) {}

    public record ReviewView(UUID id, String author, int rating, String title, String body,
                             boolean verified, Instant createdAt) {}

    public record PageResult<T>(List<T> items, long total, int page, int size, int totalPages) {}

    public record AutocompleteItem(UUID id, String name, String sku, String slug, String category, String imageUrl) {}

    public record BannerView(Long id, String title, String subtitle, String imageUrl,
                             String ctaLabel, String ctaUrl, String placement) {}

    public record PageView(Long id, String slug, String title, String body, String seoTitle, String seoDescription) {}
}
