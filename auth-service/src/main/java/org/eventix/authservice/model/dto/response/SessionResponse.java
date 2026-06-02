package org.eventix.authservice.model.dto.response;

import org.eventix.authservice.model.entity.Session;

import java.time.Instant;

public record SessionResponse(
        String id,
        Long userId,
        String status,
        Instant createdAt,
        Instant lastUsedAt,
        String ipAddress
        ) {
}
