package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.RefreshTokenException;

public class RefreshTokenReuseDetectedException extends RefreshTokenException {
    public RefreshTokenReuseDetectedException(String sessionId, Long userId) {
        super("Refresh token reuse detected for userId: " + userId + ", sessionId: " + sessionId);
    }
}