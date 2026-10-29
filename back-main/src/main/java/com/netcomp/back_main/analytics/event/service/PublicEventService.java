package com.netcomp.back_main.analytics.event.service;

import com.netcomp.back_main.analytics.event.api.request.PublicEventCreateRequest;
import com.netcomp.back_main.analytics.event.domain.Event;
import com.netcomp.back_main.analytics.event.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PublicEventService {

    private final EventRepository eventRepository;

    public PublicEventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void createEvent(PublicEventCreateRequest request) {
        Event event = new Event(
                request.sessionId(),
                request.eventType(),
                null,
                request.metadata()
        );

        eventRepository.save(event);
    }
}
