package com.eventix.ticketservice.event;

public record TicketReservedEvent(
        Long ticketId,
        Long userId,
        Long eventId,
        String sagaId
) {}