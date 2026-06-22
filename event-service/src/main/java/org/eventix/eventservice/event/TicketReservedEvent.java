package org.eventix.eventservice.event;

public record TicketReservedEvent(
        Long ticketId,
        Long eventId,
        Long userId,
        String sagaId

) {}