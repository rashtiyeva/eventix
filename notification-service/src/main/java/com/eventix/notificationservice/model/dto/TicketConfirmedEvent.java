package com.eventix.notificationservice.model.dto;

public record TicketConfirmedEvent(
        Long ticketId,
        Long userId
) {}