package com.voltmart.repository;

import com.voltmart.domain.entity.WishlistItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<WishlistItem, WishlistItem.Key> {
    @EntityGraph(attributePaths = {"product", "product.brand", "product.category", "product.images", "product.variants"})
    List<WishlistItem> findByUserId(UUID userId);
    void deleteByUserIdAndProductId(UUID userId, UUID productId);
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
}
