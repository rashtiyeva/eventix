package com.eventix.ticketservice.model.dto;

public record EventDto(
        Long id,
        Integer capacity,
        String status
) {}