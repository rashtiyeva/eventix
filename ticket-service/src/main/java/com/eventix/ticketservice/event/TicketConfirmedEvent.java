package com.eventix.ticketservice.event;

public record TicketConfirmedEvent(
        Long ticketId,
        Long userId,
        String email,
        String sagaId
) {}