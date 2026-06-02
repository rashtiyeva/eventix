package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.AccessTokenException;

public class ExpiredAccessTokenException extends AccessTokenException {
    public ExpiredAccessTokenException() {
        super("Access token expired");
    }
}
