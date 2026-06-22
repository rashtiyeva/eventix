package org.eventix.eventservice.exception;

import org.eventix.eventservice.exception.base.BadRequestException;

public class EventAlreadyCancelledException extends BadRequestException {

    public EventAlreadyCancelledException(Long id) {
        super("Event already cancelled with id: " + id);
    }
}
