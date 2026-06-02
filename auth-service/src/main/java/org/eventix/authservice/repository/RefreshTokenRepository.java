package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.RefreshToken;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;import org.eventix.authservice.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndSession_IdAndUser_Id(
            String tokenHash,
            String sessionId,
            Long userId
    );

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.revoked = true,
                rt.updatedAt = :now
            where rt.user.id = :userId
              and rt.revoked = false
            """)
    int revokeAllByUserId(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.revoked = true,
                rt.updatedAt = :now
            where rt.user.id = :userId
              and rt.session.id = :sessionId
              and rt.revoked = false
            """)
    int revokeByUserAndSessionId(
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId,
            @Param("now") Instant now
    );
}