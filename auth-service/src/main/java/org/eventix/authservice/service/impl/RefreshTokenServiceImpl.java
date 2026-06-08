package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.*;
import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.RefreshToken;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.RefreshTokenStatus;
import org.eventix.authservice.model.enums.SessionStatus;
import org.eventix.authservice.model.enums.UserStatus;
import org.eventix.authservice.repository.RefreshTokenRepository;
import org.eventix.authservice.repository.SessionRepository;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.security.JwtProperties;
import org.eventix.authservice.service.RefreshTokenService;
import org.eventix.authservice.service.SessionService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Transactional
    @Override
    public RefreshTokenResponse createToken(Long userId, String sessionId) {

        Instant now = Instant.now();

        log.debug("Creating refresh token userId={}, sessionId={}", userId, sessionId);

        String rawToken = generateToken();
        String hash = hashToken(rawToken);

        Session session = sessionRepository.findByIdAndStatus(
                sessionId,
                SessionStatus.ACTIVE
        ).orElseThrow(() -> {
            log.warn("Refresh token creation failed - session not active sessionId={}", sessionId);
            return new SessionNotActiveException(sessionId);
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Refresh token creation failed - user not found userId={}", userId);
                    return new UserNotFoundException(userId);
                });

        RefreshToken token = RefreshToken.builder()
                .tokenHash(hash)
                .user(user)
                .session(session)
                .status(RefreshTokenStatus.ACTIVE)
                .expiresAt(now.plus(jwtProperties.refreshExpiration()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        refreshTokenRepository.save(token);

        log.info("Refresh token created userId={}, sessionId={}, expiresAt={}",
                userId, sessionId, token.getExpiresAt());

        return new RefreshTokenResponse(rawToken);
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3
    )
    @Transactional
    @Override
    public RefreshTokenResponse refresh(String rawToken) {

        Instant now = Instant.now();

        log.debug("Refresh token request received");

        String hash = hashToken(rawToken);

        RefreshToken stored = refreshTokenRepository
                .findByTokenHash(hash)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found or invalid");
                    return new InvalidRefreshTokenException(rawToken);
                });

        validateTokenState(stored);

        int updated = refreshTokenRepository.markAsUsedIfActive(
                stored.getId(),
                RefreshTokenStatus.USED,
                RefreshTokenStatus.ACTIVE,
                now
        );

        if (updated == 0) {
            log.warn(
                    "Refresh token race condition or reuse detected userId={}, sessionId={}",
                    stored.getUser().getId(),
                    stored.getSession().getId()
            );
            throw new RefreshTokenAlreadyUsedException();
        }

        sessionRepository.touchSession(
                stored.getSession().getId(),
                now
        );

        String newRawToken = generateToken();

        RefreshToken newToken = RefreshToken.builder()
                .tokenHash(hashToken(newRawToken))
                .user(stored.getUser())
                .session(stored.getSession())
                .status(RefreshTokenStatus.ACTIVE)
                .expiresAt(now.plus(jwtProperties.refreshExpiration()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        refreshTokenRepository.save(newToken);

        log.info("Refresh token rotated successfully userId={}, sessionId={}",
                stored.getUser().getId(),
                stored.getSession().getId()
        );

        return new RefreshTokenResponse(newRawToken);
    }

    @Override
    @Transactional
    public void revokeAll(Long userId) {

        Instant now = Instant.now();

        log.debug("Revoking all refresh tokens userId={}", userId);

        int updated = refreshTokenRepository.updateStatusByUserId(
                userId,
                RefreshTokenStatus.REVOKED,
                RefreshTokenStatus.ACTIVE,
                now
        );

        log.info("Revoked {} refresh tokens for userId={}", updated, userId);
    }

    @Override
    @Transactional
    public void revokeSession(Long userId, String sessionId) {

        log.debug("Revoking refresh tokens for session userId={}, sessionId={}",
                userId, sessionId);

        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Revoke session failed - user not found userId={}", userId);
                    return new UserNotFoundException(userId);
                });

        Session session = sessionService.getSession(sessionId);

        int updated = refreshTokenRepository.updateStatusByUserAndSessionId(
                user.getId(),
                session.getId(),
                RefreshTokenStatus.REVOKED,
                RefreshTokenStatus.ACTIVE,
                Instant.now()
        );

        log.info("Revoked {} refresh tokens userId={}, sessionId={}",
                updated, user.getId(), session.getId());
    }

    @Transactional
    @Override
    public int markExpiredTokens() {

        Instant now = Instant.now();

        int updated = refreshTokenRepository.markExpiredTokens(now);

        log.info("Marked {} refresh tokens as expired", updated);

        return updated;
    }

    @Transactional
    @Override
    public int deleteOldTokens() {

        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        int deleted = refreshTokenRepository.deleteOldTokens(cutoff);

        log.info("Deleted {} old refresh tokens", deleted);

        return deleted;
    }

    private void validateTokenState(RefreshToken token) {

        switch (token.getStatus()) {

            case USED -> {
                Long userId = token.getUser().getId();
                String sessionId = token.getSession().getId();

                log.error(
                        "SECURITY ALERT: refresh token reuse detected (possible theft) userId={}, sessionId={}",
                        userId,
                        sessionId
                );

                sessionService.revoke(sessionId);

                throw new RefreshTokenReuseDetectedException(sessionId, userId);
            }

            case REVOKED -> {
                log.warn("Refresh token revoked userId={}, sessionId={}",
                        token.getUser().getId(),
                        token.getSession().getId()
                );

                throw new RefreshTokenRevokedException();
            }

            case EXPIRED -> {
                log.warn("Refresh token expired userId={}, sessionId={}",
                        token.getUser().getId(),
                        token.getSession().getId()
                );

                throw new RefreshTokenExpiredException();
            }

            case ACTIVE -> {
                Instant now = Instant.now();

                if (token.getExpiresAt().isBefore(now)) {

                    log.warn(
                            "Refresh token expired but cleanup has not processed it yet userId={}, sessionId={}",
                            token.getUser().getId(),
                            token.getSession().getId()
                    );

                    throw new RefreshTokenExpiredException();
                }

                log.debug("Refresh token valid userId={}, sessionId={}",
                        token.getUser().getId(),
                        token.getSession().getId()
                );
            }
        }
    }

    private String generateToken() {

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            log.error("Refresh token hashing failed - SHA-256 not available", e);
            throw new RefreshTokenHashingException();
        }
    }
}