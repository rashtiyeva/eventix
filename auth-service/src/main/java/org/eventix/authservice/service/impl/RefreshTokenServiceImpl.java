package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.*;
import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.RefreshToken;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.repository.RefreshTokenRepository;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.security.JwtProperties;
import org.eventix.authservice.service.RefreshTokenService;
import org.eventix.authservice.service.SessionService;
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
                .revoked(false)
                .expiresAt(now.plus(jwtProperties.refreshExpiration()))
                .createdAt(now)
                .build();

        refreshTokenRepository.save(token);

        return new RefreshTokenResponse(rawToken);
    }

    @Transactional
    @Override
    public RefreshTokenResponse refresh(String rawToken, Session session, User user) {

        String hash = hashToken(rawToken);
        Instant now = Instant.now();

        Session freshSession = sessionService.getActiveSession(session);

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndSession_IdAndUser_Id(hash, freshSession.getId(), user.getId())
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(rawToken, freshSession)
                );

        if (!stored.getUser().getId().equals(user.getId())) {
            throw new InvalidRefreshTokenException(rawToken, freshSession);
        }

        if (stored.getExpiresAt().isBefore(now)) {
            throw new InvalidRefreshTokenException(rawToken, freshSession);
        }

        if (stored.getUsedAt() != null) {
            refreshTokenRepository.revokeAllByUserId(
                    stored.getUser().getId(),
                    now
            );

            throw new RefreshTokenReuseDetectedException(freshSession, user.getId());
        }

        stored.setUsedAt(now);
        stored.setRevoked(true);
        stored.setUpdatedAt(now);

        refreshTokenRepository.save(stored);

        String newRaw = generateToken();
        String newHash = hashToken(newRaw);

        RefreshToken newToken = RefreshToken.builder()
                .tokenHash(newHash)
                .user(stored.getUser())
                .session(freshSession)
                .revoked(false)
                .usedAt(null)
                .expiresAt(now.plus(jwtProperties.refreshExpiration()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        refreshTokenRepository.save(newToken);

        return new RefreshTokenResponse(newRaw);
    }

    @Override
    public void revokeAll(Long userId) {

        int updated = refreshTokenRepository.revokeAllByUserId(
                userId,
                Instant.now()
        );

        log.info(
                "Revoked {} tokens for userId={}",
                updated,
                userId
        );
    }

    @Override
    public void revokeSession(Long userId, String sessionId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Session sessionRef = Session.builder()
                .id(sessionId)
                .build();

        Session freshSession = sessionService.getActiveSession(sessionRef);

        int updated = refreshTokenRepository.revokeByUserAndSessionId(
                user.getId(),
                freshSession.getId(),
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
    public boolean validate(String rawToken, Session session, User user) {

        String hash = hashToken(rawToken);
        Instant now = Instant.now();

        Session freshSession = sessionService.getActiveSession(session);

        RefreshToken token = refreshTokenRepository
                .findByTokenHashAndSession_IdAndUser_Id(hash, freshSession.getId(), user.getId())
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(rawToken, freshSession)
                );

        if (token.getExpiresAt().isBefore(now)) {
            throw new RefreshTokenExpiredException(freshSession);
        }

        if (!token.getUser().getId().equals(user.getId())) {
            throw new InvalidRefreshTokenException(rawToken, freshSession);
        }

        if (token.isRevoked() || token.getUsedAt() != null) {
            throw new RefreshTokenReuseDetectedException(freshSession, user.getId());
        }

        return true;
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