package com.eventix.ticketservice.exception;

public class TicketAlreadyExistsException extends RuntimeException {

    public TicketAlreadyExistsException(Long userId, Long eventId) {
        super("Ticket already exists for userId=" + userId + ", eventId=" + eventId);
    }
}
