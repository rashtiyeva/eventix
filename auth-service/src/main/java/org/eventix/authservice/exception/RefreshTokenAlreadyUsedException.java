package org.eventix.authservice.exception;

import org.eventix.authservice.model.entity.Session;

public class RefreshTokenAlreadyUsedException extends RuntimeException {
    public RefreshTokenAlreadyUsedException() {
        super("Refresh token already used");
    }
}
