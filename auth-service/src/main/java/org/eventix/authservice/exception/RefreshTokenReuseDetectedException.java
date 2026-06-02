package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.RefreshTokenException;
import org.eventix.authservice.model.entity.Session;

public class RefreshTokenReuseDetectedException extends RefreshTokenException {
    public RefreshTokenReuseDetectedException(Session session, Long userId) {
        super("Refresh token reuse detected for userId: " + userId + ", sessionId: " + session);
    }
}