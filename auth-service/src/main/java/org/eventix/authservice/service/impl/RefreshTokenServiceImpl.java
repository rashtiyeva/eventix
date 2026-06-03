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
@Transactional
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    @Override
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


    @Override
    public RefreshTokenResponse refresh(
            String rawToken,
            String sessionId,
            Long userId
    ) {

        String hash = hashToken(rawToken);
        Instant now = Instant.now();

        Session freshSession = sessionService.getActiveSession(sessionId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndSession_IdAndUser_Id(
                        hash,
                        freshSession.getId(),
                        user.getId()
                )
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(
                                rawToken,
                                sessionId
                        )
                );

        validateToken(stored, freshSession);

        stored.setStatus(RefreshTokenStatus.USED);
        stored.setUpdatedAt(now);

        refreshTokenRepository.save(stored);

        String newRawToken = generateToken();
        String newHash = hashToken(newRawToken);

        RefreshToken newToken = RefreshToken.builder()
                .tokenHash(newHash)
                .user(stored.getUser())
                .session(freshSession)
                .status(RefreshTokenStatus.ACTIVE)
                .expiresAt(now.plus(jwtProperties.refreshExpiration()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        refreshTokenRepository.save(newToken);

        return new RefreshTokenResponse(newRawToken);
    }

    @Override
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

    @Transactional(readOnly = true)
    @Override
    public void validate(String rawToken, String sessionId, Long userId) {

        String hash = hashToken(rawToken);

        sessionService.getActiveSession(sessionId); // only for validation side-effect

        RefreshToken token = refreshTokenRepository
                .findByTokenHashAndSession_IdAndUser_Id(
                        hash,
                        sessionId,
                        userId
                )
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(rawToken, sessionId)
                );

        validateToken(token, null);
    }

    private void validateToken(
            RefreshToken token,
            Session session
    ) {

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException(session);
        }

        switch (token.getStatus()) {

            case USED ->
                    throw new RefreshTokenAlreadyUsedException(session);

            case REVOKED ->
                    throw new RefreshTokenRevokedException(session);

            case EXPIRED ->
                    throw new RefreshTokenExpiredException(session);

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
            throw new RefreshTokenHashingException(e);
        }
    }
}