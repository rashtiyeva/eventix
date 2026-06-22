package org.eventix.eventservice.exception;

import org.eventix.eventservice.exception.base.BadRequestException;

public class EventDeletionNotAllowedException extends BadRequestException {

    public EventDeletionNotAllowedException(String message) {
        super(message);
    }
}