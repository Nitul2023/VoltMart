package com.voltmart.mapper;

import com.voltmart.domain.entity.*;
import com.voltmart.dto.CatalogDtos.*;

import java.util.List;

public final class ProductMapper {
    private ProductMapper() {}

    public static ProductCard toCard(Product product) {
        String image = product.getImages().isEmpty() ? null : product.getImages().get(0).getUrl();
        int available = product.getVariants().stream().mapToInt(ProductVariant::availableQty).sum();
        String availability = available > 0 ? (available < 5 ? "LOW_STOCK" : "IN_STOCK") : "OUT_OF_STOCK";
        return new ProductCard(
                product.getId(), product.getSku(), product.getName(), product.getSlug(),
                product.getShortDescription(), product.getBasePrice(), product.getCompareAtPrice(),
                product.getDiscountPercent(), image,
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.isFeatured(), product.getWarrantyMonths(), availability
        );
    }

    public static ProductDetail toDetail(Product product, Double avg, long reviews) {
        return new ProductDetail(
                product.getId(), product.getSku(), product.getName(), product.getSlug(),
                product.getShortDescription(), product.getDescription(),
                toBrand(product.getBrand()), toCategory(product.getCategory(), false),
                product.getBasePrice(), product.getCompareAtPrice(), product.getDiscountPercent(),
                product.getWarrantyMonths(), product.getWeightGrams(), product.isFeatured(),
                product.getSeoTitle(), product.getSeoDescription(),
                product.getImages().stream().map(i -> new ImageView(i.getId(), i.getUrl(), i.getAltText(), i.getSortOrder())).toList(),
                product.getVariants().stream().map(ProductMapper::toVariant).toList(),
                product.getSpecs().stream().map(s -> new SpecView(s.getGroupName(), s.getSpecKey(), s.getSpecValue())).toList(),
                avg, reviews
        );
    }

    public static VariantView toVariant(ProductVariant v) {
        return new VariantView(v.getId(), v.getSku(), v.getName(), v.getColor(), v.getStorage(),
                v.getOtherOption(), v.getPrice(), v.getCompareAtPrice(), v.getStockQty(),
                v.availableQty(), v.isActive());
    }

    public static BrandView toBrand(Brand b) {
        if (b == null) return null;
        return new BrandView(b.getId(), b.getName(), b.getSlug(), b.getLogoUrl(), b.getDescription());
    }

    public static CategoryView toCategory(Category c, boolean includeChildren) {
        if (c == null) return null;
        List<CategoryView> children = includeChildren
                ? c.getChildren().stream().filter(Category::isActive).map(child -> toCategory(child, false)).toList()
                : List.of();
        Long parentId = c.getParent() != null ? c.getParent().getId() : null;
        return new CategoryView(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getImageUrl(),
                parentId, children, c.getSeoTitle(), c.getSeoDescription());
    }

    public static AutocompleteItem toAutocomplete(Product p) {
        String image = p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl();
        return new AutocompleteItem(p.getId(), p.getName(), p.getSku(), p.getSlug(),
                p.getCategory() != null ? p.getCategory().getName() : null, image);
    }
}
