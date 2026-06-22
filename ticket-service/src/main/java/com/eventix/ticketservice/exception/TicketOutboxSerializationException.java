package com.eventix.ticketservice.exception;

public class TicketOutboxSerializationException extends RuntimeException {

    public TicketOutboxSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}