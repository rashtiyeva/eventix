package org.eventix.authservice.exception;

import org.eventix.authservice.model.entity.Session;

public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException(Session session) {
        super("Refresh token is revoked " + session);
    }
}
