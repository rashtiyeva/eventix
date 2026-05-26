package org.eventix.authservice.model.dto.response;

public record UserResponse(
        Long id,
        String email,
        String role
) {
}
