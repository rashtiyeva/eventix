package com.eventix.notificationservice.event;

public record TicketConfirmedEvent(
        Long ticketId,
        Long userId,
        String email,
        String sagaId
) {}