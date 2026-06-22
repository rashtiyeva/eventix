package com.eventix.ticketservice.exception;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(Long id) {
        super("Ticket not found: " + id);
    }
}