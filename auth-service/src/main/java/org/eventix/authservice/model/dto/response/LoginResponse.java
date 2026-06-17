package org.eventix.authservice.model.dto.response;

public record LoginResponse(
        boolean requires2fa,
        String tempToken,
        AuthResponse auth
) {}
