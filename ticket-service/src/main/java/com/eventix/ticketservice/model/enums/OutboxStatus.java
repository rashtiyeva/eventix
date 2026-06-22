package com.eventix.ticketservice.model.enums;

public enum OutboxStatus {
    NEW,
    PROCESSING,
    PUBLISHED,
    FAILED
}