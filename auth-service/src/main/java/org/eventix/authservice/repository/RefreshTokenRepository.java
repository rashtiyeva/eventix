package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.RefreshToken;
import org.eventix.authservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndSessionId(
            String tokenHash,
            String sessionId
    );

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.revoked = true,
                rt.updatedAt = :now
            where rt.user = :user
              and rt.revoked = false
            """)
    int revokeAllByUser(
            @Param("user") User user,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.revoked = true,
                rt.updatedAt = :now
            where rt.user = :user
              and rt.sessionId = :sessionId
              and rt.revoked = false
            """)
    int revokeByUserAndSession(
            @Param("user") User user,
            @Param("sessionId") String sessionId,
            @Param("now") Instant now
    );

}