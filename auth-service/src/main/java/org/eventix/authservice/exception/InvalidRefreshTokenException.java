package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.RefreshTokenException;

public class InvalidRefreshTokenException extends RefreshTokenException {
    public InvalidRefreshTokenException(String token, String sessionId) {
        super("Invalid refresh token. token=" + token + ", sessionId=" + sessionId);
    }
}
