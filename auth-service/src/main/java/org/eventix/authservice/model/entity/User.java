package org.eventix.authservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.model.enums.UserStatus;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 255)
    String email;

    @Column(nullable = false)
    @JsonIgnore
    String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    UserStatus status;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

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

