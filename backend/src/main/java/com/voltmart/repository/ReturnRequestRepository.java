package com.voltmart.repository;

import com.voltmart.domain.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, UUID> {
    List<ReturnRequest> findByUserId(UUID userId);
    List<ReturnRequest> findByOrderId(UUID orderId);
}
