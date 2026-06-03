package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.RefreshToken;
import org.eventix.authservice.model.enums.RefreshTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndSession_IdAndUser_IdAndStatus(
            String tokenHash,
            String sessionId,
            Long userId,
            RefreshTokenStatus status
    );

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.status = :status,
                rt.updatedAt = :now
            where rt.user.id = :userId
              and rt.status = :currentStatus
            """)
    int updateStatusByUserId(
            @Param("userId") Long userId,
            @Param("status") RefreshTokenStatus status,
            @Param("currentStatus") RefreshTokenStatus currentStatus,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.status = :status,
                rt.updatedAt = :now
            where rt.user.id = :userId
              and rt.session.id = :sessionId
              and rt.status = :currentStatus
            """)
    int updateStatusByUserAndSessionId(
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId,
            @Param("status") RefreshTokenStatus status,
            @Param("currentStatus") RefreshTokenStatus currentStatus,
            @Param("now") Instant now
    );
}