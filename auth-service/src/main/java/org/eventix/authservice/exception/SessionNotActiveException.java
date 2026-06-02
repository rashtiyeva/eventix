package org.eventix.authservice.exception;

public class SessionNotActiveException extends RuntimeException {

    public SessionNotActiveException(String sessionId) {
        super("Session is not active: " + sessionId);
    }
}