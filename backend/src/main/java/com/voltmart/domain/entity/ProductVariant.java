package com.voltmart.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, unique = true, length = 64) private String sku;
    @Column(nullable = false, length = 160) private String name;
    private String color;
    private String storage;
    @Column(name = "other_option") private String otherOption;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
    @Column(name = "compare_at_price", precision = 12, scale = 2) private BigDecimal compareAtPrice;
    @Column(name = "stock_qty", nullable = false) private int stockQty;
    @Column(name = "reserved_qty", nullable = false) private int reservedQty;
    @Column(nullable = false) private boolean active = true;

    public int availableQty() {
        return Math.max(0, stockQty - reservedQty);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    public String getOtherOption() { return otherOption; }
    public void setOtherOption(String otherOption) { this.otherOption = otherOption; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getCompareAtPrice() { return compareAtPrice; }
    public void setCompareAtPrice(BigDecimal compareAtPrice) { this.compareAtPrice = compareAtPrice; }
    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
    public int getReservedQty() { return reservedQty; }
    public void setReservedQty(int reservedQty) { this.reservedQty = reservedQty; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
