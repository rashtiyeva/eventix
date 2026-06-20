package org.eventix.eventservice.exception;

import org.eventix.eventservice.exception.base.BadRequestException;

public class EventCapacityException extends BadRequestException {

    public EventCapacityException(Integer capacity) {
        super("Invalid event capacity: " + capacity);
    }
}
