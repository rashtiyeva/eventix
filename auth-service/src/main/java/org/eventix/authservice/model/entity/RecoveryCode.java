package org.eventix.authservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "user_recovery_codes",
        indexes = {
                @Index(name = "idx_recovery_user", columnList = "user_id"),
                @Index(name = "idx_recovery_used", columnList = "used")
        }
)
public class RecoveryCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    User user;

    @Column(nullable = false, length = 255)
    String codeHash;

    @Column(nullable = false)
    boolean used = false;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column
    Instant usedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
