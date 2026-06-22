package com.eventix.ticketservice.model.dto.request;

import jakarta.validation.constraints.NotNull;

public record TicketPurchaseRequest(
        @NotNull
        Long eventId
) {}