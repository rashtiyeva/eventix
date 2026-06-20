package org.eventix.eventservice.exception;

import org.eventix.eventservice.exception.base.BadRequestException;

public class InvalidEventStateException extends BadRequestException {

    public InvalidEventStateException(String message) {
        super(message);
    }

    public static InvalidEventStateException notDraft() {
        return new InvalidEventStateException("Only DRAFT events can be published");
    }

    public static InvalidEventStateException cancelled() {
        return new InvalidEventStateException("Cancelled event cannot be modified");
    }
}