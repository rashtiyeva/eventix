package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.NotFoundException;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String sessionId) {
        super("Session not found: " + sessionId);
    }
}
