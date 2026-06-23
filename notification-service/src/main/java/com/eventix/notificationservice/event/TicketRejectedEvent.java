package com.eventix.notificationservice.event;

public record TicketRejectedEvent(
        Long ticketId,
        Long userId,
        Long eventId,
        String email,
        String reason,
        String sagaId
) {}
