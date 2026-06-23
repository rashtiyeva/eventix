package com.eventix.notificationservice.event;

import java.time.Instant;

public record UserRegisteredEvent(
        Long userId,
        String email,
        String role,
        Instant createdAt
) {}