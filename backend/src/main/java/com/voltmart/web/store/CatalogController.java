package com.voltmart.web.store;

import com.voltmart.dto.CatalogDtos.*;
import com.voltmart.dto.CommerceDtos.ReviewRequest;
import com.voltmart.service.AccountService;
import com.voltmart.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalogService;
    private final AccountService accountService;

    public CatalogController(CatalogService catalogService, AccountService accountService) {
        this.catalogService = catalogService;
        this.accountService = accountService;
    }

    @GetMapping("/categories")
    public List<CategoryView> categories() {
        return catalogService.categoryTree();
    }

    @GetMapping("/brands")
    public List<BrandView> brands() {
        return catalogService.brands();
    }

    @GetMapping("/banners")
    public List<BannerView> banners(@RequestParam(defaultValue = "HOME_HERO") String placement) {
        return catalogService.banners(placement);
    }

    @GetMapping("/pages/{slug}")
    public PageView page(@PathVariable String slug) {
        return catalogService.page(slug);
    }

    @GetMapping("/products/featured")
    public List<ProductCard> featured() {
        return catalogService.featured();
    }

    @GetMapping("/search")
    public PageResult<ProductCard> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String filters,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return catalogService.search(q, category, brand, featured, minPrice, maxPrice, filters, sort, page, size);
    }

    @GetMapping("/search/autocomplete")
    public List<AutocompleteItem> autocomplete(@RequestParam String q) {
        return catalogService.autocomplete(q);
    }

    @GetMapping("/products/{slug}")
    public ProductDetail product(@PathVariable String slug) {
        return catalogService.product(slug);
    }

    @GetMapping("/products/compare")
    public List<ProductDetail> compare(@RequestParam List<UUID> ids) {
        return catalogService.compare(ids);
    }

    @GetMapping("/categories/{slug}/filters")
    public List<FilterView> filters(@PathVariable String slug) {
        return catalogService.filters(slug);
    }

    @GetMapping("/products/{id}/reviews")
    public PageResult<ReviewView> reviews(@PathVariable UUID id,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return catalogService.reviews(id, page, size);
    }

    @PostMapping("/products/{id}/reviews")
    public void addReview(@PathVariable UUID id, @Valid @RequestBody ReviewRequest request) {
        accountService.addReview(id, request);
    }
}
