package org.eventix.authservice.exception;

public class TooMany2faAttemptsException extends RuntimeException {
    public TooMany2faAttemptsException() {
        super("Too many 2FA attempts");
    }
}
