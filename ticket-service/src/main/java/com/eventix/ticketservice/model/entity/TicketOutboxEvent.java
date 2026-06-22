package com.eventix.ticketservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.eventix.ticketservice.model.enums.OutboxStatus;

import java.time.Instant;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "ticket_outbox")
public class TicketOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "aggregate_id", nullable = false)
    Long aggregateId;

    @Column(name = "saga_id", nullable = false, length = 36)
    String sagaId;

    @Column(name = "event_type", nullable = false)
    String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    OutboxStatus status;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}
