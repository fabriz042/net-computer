package com.netcomp.back_main.analytics.event.api.controller;

import com.netcomp.back_main.analytics.event.api.request.CreateEventRequest;
import com.netcomp.back_main.analytics.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;

    public PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateEventRequest request) {
        eventService.createEvent(request);
        return ResponseEntity.noContent().build();
    }
}
