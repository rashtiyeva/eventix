package com.eventix.notificationservice.event;

public record TicketReservedEvent(
        Long ticketId,
        Long eventId,
        Long userId,
        String email,
        String sagaId
) {}