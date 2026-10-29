package com.netcomp.back_main.analytics.event.api.request;

import com.netcomp.back_main.analytics.event.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record PublicEventCreateRequest(
        @NotBlank
        @Size(max = 64)
        String sessionId,
        @NotNull
        EventType eventType,
        Map<String, Object> metadata
) {
}
