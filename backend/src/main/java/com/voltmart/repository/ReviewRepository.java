package com.voltmart.repository;

import com.voltmart.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    @EntityGraph(attributePaths = "user")
    Page<Review> findByProductId(UUID productId, Pageable pageable);
    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double averageRating(@Param("productId") UUID productId);

    long countByProductId(UUID productId);
}
