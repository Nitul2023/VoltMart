package com.voltmart.web.store;

import com.voltmart.domain.entity.AnalyticsEvent;
import com.voltmart.repository.AnalyticsEventRepository;
import com.voltmart.util.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsEventRepository repository;

    public AnalyticsController(AnalyticsEventRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/events")
    public void track(@RequestBody Map<String, String> body) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventName(body.getOrDefault("eventName", "page_view"));
        event.setPath(body.get("path"));
        event.setSessionId(body.get("sessionId"));
        event.setMetadata(body.get("metadata"));
        CurrentUser.principal().ifPresent(p -> event.setUserId(p.getId()));
        repository.save(event);
    }
}
