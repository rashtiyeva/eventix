package org.eventix.eventservice.model.dto.response;

import org.eventix.eventservice.model.enums.EventStatus;

import java.time.LocalDateTime;

public record EventPreviewResponse(
        Long id,
        String title,
        String category,
        String location,
        LocalDateTime startTime,
        EventStatus status
) {}
