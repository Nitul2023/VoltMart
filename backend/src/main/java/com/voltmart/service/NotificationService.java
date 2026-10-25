package com.voltmart.service;

import com.voltmart.domain.entity.EmailNotification;
import com.voltmart.repository.EmailNotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final EmailNotificationRepository repository;

    public NotificationService(EmailNotificationRepository repository) {
        this.repository = repository;
    }

    public void send(String recipient, String subject, String body, String relatedType, String relatedId) {
        EmailNotification n = new EmailNotification();
        n.setRecipient(recipient);
        n.setSubject(subject);
        n.setBody(body);
        n.setStatus("QUEUED");
        n.setRelatedType(relatedType);
        n.setRelatedId(relatedId);
        repository.save(n);
        n.setStatus("SENT");
        repository.save(n);
    }
}
