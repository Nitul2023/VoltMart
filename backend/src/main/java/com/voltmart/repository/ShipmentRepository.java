package com.voltmart.repository;

import com.voltmart.domain.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    List<Shipment> findByOrderId(UUID orderId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
