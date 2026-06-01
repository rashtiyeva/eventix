package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.AccessTokenException;

public class InvalidAccessTokenException extends AccessTokenException {
    public InvalidAccessTokenException() {
        super("Invalid access token");
    }
}
