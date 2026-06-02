package org.eventix.authservice.model.dto.request;

public record CreateSessionRequest(
        String ip,
        String userAgent
) {}
