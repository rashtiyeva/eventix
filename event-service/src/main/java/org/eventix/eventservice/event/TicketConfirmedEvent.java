package org.eventix.eventservice.event;

public record TicketConfirmedEvent(
        Long ticketId,
        Long userId,
        Long eventId,
        String sagaId
) {}