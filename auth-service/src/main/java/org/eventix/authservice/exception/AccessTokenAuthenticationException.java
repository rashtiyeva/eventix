package org.eventix.authservice.exception;

public class AccessTokenAuthenticationException extends RuntimeException {

    public AccessTokenAuthenticationException(String reason) {
        super("JWT authentication failed: " + reason);
    }
}