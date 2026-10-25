package com.voltmart.repository;

import com.voltmart.domain.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlugAndActiveTrue(String slug);
    org.springframework.data.domain.Page<Product> findAllByActiveTrue(org.springframework.data.domain.Pageable pageable);
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, UUID id);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Product> findByFeaturedTrueAndActiveTrue();

    @Query("""
        SELECT p FROM Product p
        WHERE p.active = true AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        ORDER BY p.name
        """)
    List<Product> autocomplete(@Param("q") String q, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.active = true")
    List<Product> findActiveByIds(@Param("ids") List<UUID> ids);
}
