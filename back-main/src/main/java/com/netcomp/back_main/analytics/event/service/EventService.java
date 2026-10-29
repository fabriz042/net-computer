package com.netcomp.back_main.analytics.event.service;

import com.netcomp.back_main.analytics.event.api.request.CreateEventRequest;
import com.netcomp.back_main.analytics.event.domain.Event;
import com.netcomp.back_main.analytics.event.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void createEvent(CreateEventRequest request) {
        Event event = new Event(
                request.sessionId(),
                request.eventType(),
                null,
                request.metadata()
        );

        eventRepository.save(event);
    }
}
