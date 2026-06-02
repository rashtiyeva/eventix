package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.RefreshTokenException;
import org.eventix.authservice.model.entity.Session;

public class InvalidRefreshTokenException extends RefreshTokenException {
    public InvalidRefreshTokenException(String token, Session session) {
        super("Invalid refresh token. token=" + token + ", sessionId=" + session);
    }
}
