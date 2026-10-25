package com.voltmart.domain.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "wishlists")
@IdClass(WishlistItem.Key.class)
public class WishlistItem {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt = Instant.now();

    public static class Key implements Serializable {
        private UUID userId;
        private UUID productId;
        public Key() {}
        public Key(UUID userId, UUID productId) { this.userId = userId; this.productId = productId; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key k)) return false;
            return Objects.equals(userId, k.userId) && Objects.equals(productId, k.productId);
        }
        @Override public int hashCode() { return Objects.hash(userId, productId); }
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
}
