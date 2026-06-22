package com.eventix.ticketservice.event;

public record TicketRejectedEvent(
        Long ticketId,
        Long userId,
        Long eventId,
        String reason,
        String sagaId
) {}