package org.eventix.authservice.exception;

public class AccessTokenAuthenticationException extends RuntimeException {
    public AccessTokenAuthenticationException() {
        super("JWT authentication failed");
    }
}