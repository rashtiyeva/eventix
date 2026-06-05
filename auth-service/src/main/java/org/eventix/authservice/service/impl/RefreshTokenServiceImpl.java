package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.*;
import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.RefreshToken;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.RefreshTokenStatus;
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

    @Override
    @Transactional
    public RefreshTokenResponse createToken(User user, Session session) {

        Instant now = Instant.now();

        String rawToken = generateToken();
        String hash = hashToken(rawToken);

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

        String hash = hashToken(rawToken);

        RefreshToken stored = refreshTokenRepository
                .findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException(rawToken));

        validateTokenState(stored);

        int updated = refreshTokenRepository.markAsUsedIfActive(
                stored.getId(),
                RefreshTokenStatus.USED,
                RefreshTokenStatus.ACTIVE,
                now
        );

        if (updated == 0) {
            log.warn("Concurrent refresh detected or token already used. userId={}, sessionId={}",
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

        return new RefreshTokenResponse(newRawToken);
    }

    @Override
    @Transactional
    public void revokeAll(Long userId) {

        Instant now = Instant.now();

        int updated = refreshTokenRepository.updateStatusByUserId(
                userId,
                RefreshTokenStatus.REVOKED,
                RefreshTokenStatus.ACTIVE,
                now
        );

        log.info(
                "Revoked {} tokens for userId={}",
                updated,
                userId
        );
    }

    @Override
    @Transactional
    public void revokeSession(Long userId, String sessionId) {

        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Session freshSession = sessionService.getActiveSession(sessionId);

        int updated = refreshTokenRepository.updateStatusByUserAndSessionId(
                user.getId(),
                freshSession.getId(),
                RefreshTokenStatus.REVOKED,
                RefreshTokenStatus.ACTIVE,
                Instant.now()
        );

        log.info(
                "Revoked {} tokens for userId={}, sessionId={}",
                updated,
                user.getId(),
                freshSession.getId()
        );
    }

    private RefreshToken resolveAndValidate(String rawToken) {

        String hash = hashToken(rawToken);

        RefreshToken token = refreshTokenRepository
                .findByTokenHash(hash)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(rawToken)
                );

        validateTokenState(token);

        return token;
    }

    private void validateTokenState(RefreshToken token) {

        Instant now = Instant.now();

        if (token.getExpiresAt().isBefore(now)) {
            throw new RefreshTokenExpiredException();
        }

        switch (token.getStatus()) {

            case USED -> {

                Long userId = token.getUser().getId();
                String sessionId = token.getSession().getId();

                log.warn("Refresh token reuse detected! possible theft. userId={}, sessionId={}",
                        token.getUser().getId(),
                        token.getSession().getId()
                );

                sessionService.revoke(token.getSession());

                throw new RefreshTokenReuseDetectedException(sessionId, userId);
            }
            case REVOKED -> throw new RefreshTokenRevokedException();
            case EXPIRED -> throw new RefreshTokenExpiredException();
            case ACTIVE -> { }
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
            throw new RefreshTokenHashingException();
        }
    }
}