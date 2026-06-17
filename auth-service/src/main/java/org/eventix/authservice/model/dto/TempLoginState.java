package org.eventix.authservice.model.dto;


import java.time.Instant;


public record TempLoginState(
        Long userId,
        String deviceId,
        String ip,
        String userAgent,
        int attempts,
        Instant createdAt
) {}