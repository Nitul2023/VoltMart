package com.voltmart.service;

import com.voltmart.domain.entity.AuditLog;
import com.voltmart.repository.AuditLogRepository;
import com.voltmart.util.CurrentUser;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String entityType, String entityId, String details) {
        AuditLog log = new AuditLog();
        CurrentUser.principal().ifPresent(p -> {
            log.setActorId(p.getId());
            log.setActorEmail(p.getUsername());
        });
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
