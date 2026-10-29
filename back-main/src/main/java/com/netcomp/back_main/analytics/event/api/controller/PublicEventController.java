package com.netcomp.back_main.analytics.event.api.controller;

import com.netcomp.back_main.analytics.event.api.request.PublicEventCreateRequest;
import com.netcomp.back_main.analytics.event.service.PublicEventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class PublicEventController {

    private final PublicEventService eventService;

    public PublicEventController(PublicEventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody PublicEventCreateRequest request) {
        eventService.createEvent(request);
        return ResponseEntity.noContent().build();
    }
}
