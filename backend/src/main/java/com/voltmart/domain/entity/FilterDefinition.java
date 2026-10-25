package com.voltmart.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "filter_definitions")
public class FilterDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 80) private String name;
    @Column(nullable = false, length = 100) private String slug;
    @Column(name = "filter_type", nullable = false, length = 20) private String filterType;
    private String unit;
    @Column(name = "sort_order", nullable = false) private int sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getFilterType() { return filterType; }
    public void setFilterType(String filterType) { this.filterType = filterType; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
