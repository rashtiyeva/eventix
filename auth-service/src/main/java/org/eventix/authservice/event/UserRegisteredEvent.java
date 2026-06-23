package org.eventix.authservice.event;

import java.time.Instant;

public record UserRegisteredEvent(
        Long userId,
        String email,
        String role,
        Instant createdAt
) {}