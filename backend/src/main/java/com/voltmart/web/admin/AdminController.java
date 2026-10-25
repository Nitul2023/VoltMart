package com.voltmart.web.admin;

import com.voltmart.domain.entity.*;
import com.voltmart.domain.enums.OrderStatus;
import com.voltmart.dto.AdminDtos.*;
import com.voltmart.dto.CatalogDtos.ProductDetail;
import com.voltmart.dto.CommerceDtos.OrderView;
import com.voltmart.dto.CommerceDtos.ReturnView;
import com.voltmart.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/products")
    public Page<ProductDetail> products(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return adminService.productDetails(page, size);
    }

    @PostMapping("/products")
    public ProductDetail createProduct(@Valid @RequestBody ProductWrite write) {
        return adminService.saveProduct(null, write);
    }

    @PutMapping("/products/{id}")
    public ProductDetail updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductWrite write) {
        return adminService.saveProduct(id, write);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable UUID id) {
        adminService.deleteProduct(id);
    }

    @PostMapping("/inventory")
    public void inventory(@Valid @RequestBody InventoryAdjust adjust) {
        adminService.adjustInventory(adjust);
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return adminService.allCategories();
    }

    @PostMapping("/categories")
    public Category createCategory(@Valid @RequestBody CategoryWrite write) {
        return adminService.saveCategory(null, write);
    }

    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryWrite write) {
        return adminService.saveCategory(id, write);
    }

    @GetMapping("/brands")
    public List<Brand> brands() {
        return adminService.allBrands();
    }

    @PostMapping("/brands")
    public Brand createBrand(@Valid @RequestBody BrandWrite write) {
        return adminService.saveBrand(null, write);
    }

    @PutMapping("/brands/{id}")
    public Brand updateBrand(@PathVariable Long id, @Valid @RequestBody BrandWrite write) {
        return adminService.saveBrand(id, write);
    }

    @GetMapping("/banners")
    public List<Banner> banners() {
        return adminService.allBanners();
    }

    @PostMapping("/banners")
    public Banner createBanner(@Valid @RequestBody BannerWrite write) {
        return adminService.saveBanner(null, write);
    }

    @PutMapping("/banners/{id}")
    public Banner updateBanner(@PathVariable Long id, @Valid @RequestBody BannerWrite write) {
        return adminService.saveBanner(id, write);
    }

    @GetMapping("/pages")
    public List<CmsPage> pages() {
        return adminService.allPages();
    }

    @PostMapping("/pages")
    public CmsPage createPage(@Valid @RequestBody PageWrite write) {
        return adminService.savePage(null, write);
    }

    @PutMapping("/pages/{id}")
    public CmsPage updatePage(@PathVariable Long id, @Valid @RequestBody PageWrite write) {
        return adminService.savePage(id, write);
    }

    @GetMapping("/coupons")
    public List<Coupon> coupons() {
        return adminService.allCoupons();
    }

    @PostMapping("/coupons")
    public Coupon createCoupon(@Valid @RequestBody CouponWrite write) {
        return adminService.saveCoupon(null, write);
    }

    @PutMapping("/coupons/{id}")
    public Coupon updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponWrite write) {
        return adminService.saveCoupon(id, write);
    }

    @GetMapping("/promotions")
    public List<Promotion> promotions() {
        return adminService.allPromotions();
    }

    @PostMapping("/promotions")
    public Promotion createPromotion(@Valid @RequestBody PromotionWrite write) {
        return adminService.savePromotion(null, write);
    }

    @GetMapping("/orders")
    public Page<OrderView> orders(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) OrderStatus status) {
        return adminService.orderViews(page, size, status);
    }

    @PatchMapping("/orders/{id}")
    public OrderView updateOrder(@PathVariable UUID id, @Valid @RequestBody OrderStatusWrite write) {
        return adminService.updateOrder(id, write);
    }

    @GetMapping("/returns")
    public List<ReturnView> returns() {
        return adminService.returnViews();
    }

    @PostMapping("/returns/{id}/resolve")
    public void resolveReturn(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        adminService.resolveReturn(id, Boolean.TRUE.equals(body.get("approve")));
    }

    @GetMapping("/customers")
    public List<CustomerView> customers() {
        return adminService.customers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/customers/{id}")
    public void setEnabled(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        adminService.setCustomerEnabled(id, Boolean.TRUE.equals(body.get("enabled")));
    }

    @GetMapping("/reports/summary")
    public ReportSummary summary() {
        return adminService.report();
    }

    @GetMapping("/audit-logs")
    public Page<AuditView> audits(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "30") int size) {
        return adminService.audits(page, size).map(a ->
                new AuditView(a.getId(), a.getActorEmail(), a.getAction(), a.getEntityType(),
                        a.getEntityId(), a.getDetails(), a.getCreatedAt()));
    }
}
