package org.eventix.authservice.model.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String role,
        String status,
        LocalDateTime createdAt
) {
}
