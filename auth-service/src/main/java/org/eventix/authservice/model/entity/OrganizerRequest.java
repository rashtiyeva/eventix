package org.eventix.authservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.eventix.authservice.model.enums.RequestStatus;

import java.time.Instant;

@Entity
@Table(name = "organizer_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = RequestStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}