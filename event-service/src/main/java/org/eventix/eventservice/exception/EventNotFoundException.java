package org.eventix.eventservice.exception;

import org.eventix.eventservice.exception.base.NotFoundException;

public class EventNotFoundException extends NotFoundException {

    public EventNotFoundException(Long id) {
        super("Event not found with id: " + id);
    }
}