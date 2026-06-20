package org.eventix.eventservice.exception;

import org.eventix.eventservice.exception.base.ForbiddenException;

public class EventAccessDeniedException extends ForbiddenException {

    public EventAccessDeniedException(Long eventId, Long userId) {
        super("User " + userId + " has no access to event " + eventId);
    }
}
