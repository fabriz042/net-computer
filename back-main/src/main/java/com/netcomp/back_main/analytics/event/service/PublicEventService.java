package com.netcomp.back_main.analytics.event.service;

import com.netcomp.back_main.analytics.event.api.request.PublicEventCreateRequest;
import com.netcomp.back_main.analytics.event.domain.Event;
import com.netcomp.back_main.analytics.event.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PublicEventService {

    // Constants

    // Dependencies
    private final EventRepository eventRepository;

    // Constructor
    public PublicEventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Public operations
    @Transactional
    public void createEvent(PublicEventCreateRequest request) {
        Event event = new Event(
                request.sessionId(),
                request.visitorId(),
                request.eventType(),
                null,
                request.metadata()
        );

        eventRepository.save(event);
    }

    // Private helpers
}
