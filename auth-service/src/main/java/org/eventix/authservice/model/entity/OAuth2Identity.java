package org.eventix.authservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.eventix.authservice.model.enums.OAuthProvider;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "oauth2_identities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_provider_user_id",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
public class OAuth2Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    String providerUserId;

    @Column(length = 255)
    String email;

    @Column(nullable = false)
    Boolean emailVerified;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant updatedAt;

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
