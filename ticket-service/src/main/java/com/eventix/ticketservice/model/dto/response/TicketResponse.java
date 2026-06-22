package com.eventix.ticketservice.model.dto.response;

import com.eventix.ticketservice.model.enums.TicketStatus;

import java.time.Instant;

public record TicketResponse(
        Long ticketId,
        Long eventId,
        TicketStatus status,
        Instant expiresAt
) {}