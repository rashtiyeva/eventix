package org.eventix.eventservice.exception;

import org.eventix.eventservice.exception.base.BadRequestException;

public class EventUpdateNotAllowedException extends BadRequestException {

    public EventUpdateNotAllowedException(String message) {
        super(message);
    }
}