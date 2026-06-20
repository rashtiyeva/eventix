package org.eventix.eventservice.model.dto.request;

import java.time.LocalDateTime;

public record EventCreateRequest(
        String title,
        String description,
        String category,
        String location,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer capacity
) {}