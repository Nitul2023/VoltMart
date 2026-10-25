package com.voltmart.repository;

import com.voltmart.domain.entity.Order;
import com.voltmart.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "items")
    Page<Order> findByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    long countByCreatedAtAfter(Instant after);
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status <> com.voltmart.domain.enums.OrderStatus.CANCELLED AND o.createdAt >= :after")
    BigDecimal revenueSince(@org.springframework.data.repository.query.Param("after") Instant after);
}
