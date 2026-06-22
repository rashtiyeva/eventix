package com.eventix.ticketservice.event;

public record TicketConfirmedEvent(
        Long ticketId,
        Long userId,
        Long eventId,
        String sagaId
) {}