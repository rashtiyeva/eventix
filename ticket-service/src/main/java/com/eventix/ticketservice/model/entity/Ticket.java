package com.eventix.ticketservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.eventix.ticketservice.model.enums.TicketStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "event_id"})
        }
)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "event_id", nullable = false)
    Long eventId;

    @Column(name = "saga_id", nullable = false, updatable = false, length = 36)
    String sagaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TicketStatus status;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant expiresAt;

    @Version
    Long version;

    @Column(nullable = false)
    String email;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();

        if (this.expiresAt == null) {
            this.expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);
        }
        if (this.sagaId == null) {
            this.sagaId = UUID.randomUUID().toString();
        }
    }
}
