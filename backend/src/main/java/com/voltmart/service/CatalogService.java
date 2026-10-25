package com.voltmart.service;

import com.voltmart.domain.entity.Category;
import com.voltmart.domain.entity.Product;
import com.voltmart.dto.CatalogDtos.*;
import com.voltmart.exception.ApiException;
import com.voltmart.mapper.ProductMapper;
import com.voltmart.repository.*;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CatalogService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ReviewRepository reviewRepository;
    private final FilterDefinitionRepository filterDefinitionRepository;
    private final BannerRepository bannerRepository;
    private final CmsPageRepository cmsPageRepository;

    public CatalogService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          BrandRepository brandRepository, ReviewRepository reviewRepository,
                          FilterDefinitionRepository filterDefinitionRepository,
                          BannerRepository bannerRepository, CmsPageRepository cmsPageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.reviewRepository = reviewRepository;
        this.filterDefinitionRepository = filterDefinitionRepository;
        this.bannerRepository = bannerRepository;
        this.cmsPageRepository = cmsPageRepository;
    }

    public List<CategoryView> categoryTree() {
        return categoryRepository.findTreeRoots().stream()
                .filter(Category::isActive)
                .map(c -> ProductMapper.toCategory(c, true))
                .toList();
    }

    public List<BrandView> brands() {
        return brandRepository.findByActiveTrueOrderByNameAsc().stream().map(ProductMapper::toBrand).toList();
    }

    public List<BannerView> banners(String placement) {
        return bannerRepository.findByActiveTrueAndPlacementOrderBySortOrderAsc(placement).stream()
                .map(b -> new BannerView(b.getId(), b.getTitle(), b.getSubtitle(), b.getImageUrl(),
                        b.getCtaLabel(), b.getCtaUrl(), b.getPlacement()))
                .toList();
    }

    public PageView page(String slug) {
        return cmsPageRepository.findBySlugAndPublishedTrue(slug)
                .map(p -> new PageView(p.getId(), p.getSlug(), p.getTitle(), p.getBody(), p.getSeoTitle(), p.getSeoDescription()))
                .orElseThrow(() -> ApiException.notFound("Page not found"));
    }

    public List<ProductCard> featured() {
        return productRepository.findByFeaturedTrueAndActiveTrue().stream().map(ProductMapper::toCard).toList();
    }

    public List<AutocompleteItem> autocomplete(String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        return productRepository.autocomplete(q.trim(), PageRequest.of(0, 8)).stream()
                .map(ProductMapper::toAutocomplete)
                .toList();
    }

    public PageResult<ProductCard> search(String q, String categorySlug, String brandSlug, Boolean featured,
                                          BigDecimal minPrice, BigDecimal maxPrice, String filtersRaw,
                                          String sort, int page, int size) {
        List<Long> categoryIds = resolveCategoryIds(categorySlug);
        Sort springSort = resolveSort(sort);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 48), springSort);
        Specification<Product> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("brand", JoinType.LEFT);
                root.fetch("category", JoinType.LEFT);
                root.fetch("images", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("sku")), like),
                        cb.like(cb.lower(root.get("category").get("name")), like),
                        cb.like(cb.lower(root.get("shortDescription")), like)
                ));
            }
            if (!categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }
            if (brandSlug != null && !brandSlug.isBlank()) {
                predicates.add(cb.equal(root.get("brand").get("slug"), brandSlug));
            }
            if (Boolean.TRUE.equals(featured)) {
                predicates.add(cb.isTrue(root.get("featured")));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
            }
            Map<String, String> applied = parseFilters(filtersRaw);
            for (Map.Entry<String, String> entry : applied.entrySet()) {
                Join<Object, Object> fv = root.join("filterValues");
                predicates.add(cb.and(
                        cb.equal(fv.get("filter").get("slug"), entry.getKey()),
                        cb.equal(cb.lower(fv.get("valueText")), entry.getValue().toLowerCase())
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<Product> result = productRepository.findAll(spec, pageable);
        List<ProductCard> items = result.getContent().stream().map(ProductMapper::toCard).toList();
        return new PageResult<>(items, result.getTotalElements(), result.getNumber(), result.getSize(), result.getTotalPages());
    }

    public ProductDetail product(String slug) {
        Product product = productRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> ApiException.notFound("Product not found"));
        Double avg = reviewRepository.averageRating(product.getId());
        long count = reviewRepository.countByProductId(product.getId());
        return ProductMapper.toDetail(product, avg, count);
    }

    public List<ProductDetail> compare(List<UUID> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 4) {
            throw ApiException.badRequest("Select 1 to 4 products to compare");
        }
        return productRepository.findActiveByIds(ids).stream()
                .map(p -> ProductMapper.toDetail(p, reviewRepository.averageRating(p.getId()),
                        reviewRepository.countByProductId(p.getId())))
                .toList();
    }

    public List<FilterView> filters(String categorySlug) {
        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> ApiException.notFound("Category not found"));
        List<Long> ids = new ArrayList<>();
        ids.add(category.getId());
        if (category.getParent() != null) {
            ids.add(category.getParent().getId());
        }
        return filterDefinitionRepository.findByCategoryIdInOrderBySortOrderAsc(ids).stream()
                .map(f -> new FilterView(f.getId(), f.getName(), f.getSlug(), f.getFilterType(), f.getUnit()))
                .toList();
    }

    public PageResult<ReviewView> reviews(UUID productId, int page, int size) {
        Page<com.voltmart.domain.entity.Review> result =
                reviewRepository.findByProductId(productId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<ReviewView> items = result.getContent().stream()
                .map(r -> new ReviewView(r.getId(), r.getUser().getFirstName(), r.getRating(), r.getTitle(),
                        r.getBody(), r.isVerified(), r.getCreatedAt()))
                .toList();
        return new PageResult<>(items, result.getTotalElements(), result.getNumber(), result.getSize(), result.getTotalPages());
    }

    private List<Long> resolveCategoryIds(String categorySlug) {
        if (categorySlug == null || categorySlug.isBlank()) {
            return List.of();
        }
        return categoryRepository.findBySlug(categorySlug).map(category -> {
            List<Long> ids = new ArrayList<>();
            ids.add(category.getId());
            categoryRepository.findByParentId(category.getId()).forEach(child -> ids.add(child.getId()));
            return ids;
        }).orElse(List.of(-1L));
    }

    private Map<String, String> parseFilters(String filtersRaw) {
        Map<String, String> applied = new LinkedHashMap<>();
        if (filtersRaw == null || filtersRaw.isBlank()) {
            return applied;
        }
        for (String part : filtersRaw.split(",")) {
            String[] kv = part.split(":", 2);
            if (kv.length == 2 && !kv[0].isBlank() && !kv[1].isBlank()) {
                applied.put(kv[0].trim(), kv[1].trim());
            }
        }
        return applied;
    }

    private Sort resolveSort(String sort) {
        if (sort == null) {
            return Sort.by("featured").descending().and(Sort.by("name").ascending());
        }
        return switch (sort) {
            case "price_asc" -> Sort.by("basePrice").ascending();
            case "price_desc" -> Sort.by("basePrice").descending();
            case "name" -> Sort.by("name").ascending();
            case "newest" -> Sort.by("createdAt").descending();
            default -> Sort.by("featured").descending().and(Sort.by("name").ascending());
        };
    }
}
