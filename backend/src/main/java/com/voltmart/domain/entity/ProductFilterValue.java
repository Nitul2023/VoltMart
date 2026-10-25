package com.voltmart.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_filter_values")
public class ProductFilterValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id")
    private FilterDefinition filter;

    @Column(name = "value_text", nullable = false, length = 160)
    private String valueText;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public FilterDefinition getFilter() { return filter; }
    public void setFilter(FilterDefinition filter) { this.filter = filter; }
    public String getValueText() { return valueText; }
    public void setValueText(String valueText) { this.valueText = valueText; }
}
