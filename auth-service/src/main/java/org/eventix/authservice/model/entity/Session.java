package org.eventix.authservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.eventix.authservice.model.enums.SessionStatus;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "sessions")
public class Session {

    @Id
    @Column(length = 36)
    String id;

    @Column(nullable = false)
    Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SessionStatus status;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant lastUsedAt;

    @Column(length = 255)
    String ipAddress;

    @Column(length = 512)
    String userAgent;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.lastUsedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUsedAt = Instant.now();
    }
}