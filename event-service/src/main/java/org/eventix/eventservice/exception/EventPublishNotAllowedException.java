package org.eventix.eventservice.exception;

import org.eventix.eventservice.exception.base.BadRequestException;

public class EventPublishNotAllowedException extends BadRequestException {

    public EventPublishNotAllowedException(String message) {
        super(message);
    }
}