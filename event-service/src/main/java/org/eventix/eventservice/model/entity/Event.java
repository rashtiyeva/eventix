package org.eventix.eventservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.eventix.eventservice.model.enums.EventStatus;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 255)
    String title;

    @Column(nullable = false, length = 5000)
    String description;

    @Column(nullable = false, length = 100)
    String category;

    @Column(nullable = false)
    String location;

    @Column(nullable = false)
    LocalDateTime startTime;

    @Column(nullable = false)
    LocalDateTime endTime;

    @Column(nullable = false)
    Integer capacity;

    @Column(nullable = false)
    Integer reserved = 0;

    @Column(nullable = false)
    Long organizerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EventStatus status;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant updatedAt;

    @Column
    Instant deletedAt;

    @Version
    @Column(nullable = false)
    Long version;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
