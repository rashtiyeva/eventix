package org.eventix.authservice.event;

import java.time.Instant;

public record UserDeletedEvent(
        Long userId,
        Instant deletedAt
) {}