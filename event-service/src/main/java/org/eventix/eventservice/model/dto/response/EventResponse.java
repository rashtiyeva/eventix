package org.eventix.eventservice.model.dto.response;

import org.eventix.eventservice.model.enums.EventStatus;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        String category,
        String location,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer capacity,
        Long organizerId,
        EventStatus status
) {}
