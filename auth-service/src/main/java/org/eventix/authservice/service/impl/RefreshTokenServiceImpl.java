package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.InvalidRefreshTokenException;
import org.eventix.authservice.exception.RefreshTokenExpiredException;
import org.eventix.authservice.exception.RefreshTokenHashingException;
import org.eventix.authservice.exception.RefreshTokenReuseDetectedException;
import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.RefreshToken;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.repository.RefreshTokenRepository;
import org.eventix.authservice.security.JwtProperties;
import org.eventix.authservice.service.RefreshTokenService;
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

    @Override
    public RefreshTokenResponse createToken(User user, String sessionId) {

        Instant now = Instant.now();

        String rawToken = generateToken();
        String hash = hashToken(rawToken);

        RefreshToken token = RefreshToken.builder()
                .tokenHash(hash)
                .user(user)
                .sessionId(sessionId)
                .revoked(false)
                .expiresAt(now.plus(jwtProperties.refreshExpiration()))
                .createdAt(now)
                .build();

        refreshTokenRepository.save(token);

        return new RefreshTokenResponse(rawToken);
    }

    @Transactional
    @Override
    public RefreshTokenResponse refresh(String rawToken, String sessionId, User user) {

        String hash = hashToken(rawToken);
        Instant now = Instant.now();

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndSessionId(hash, sessionId)
                .orElseThrow(() -> new InvalidRefreshTokenException(rawToken, sessionId));

        if (stored.getUsedAt() != null) {
            refreshTokenRepository.revokeAllByUser(stored.getUser(), now);
            throw new RefreshTokenReuseDetectedException(sessionId, user.getId());
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
                .sessionId(sessionId)
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
    public void revokeAll(User user) {

        int updated = refreshTokenRepository.revokeAllByUser(
                user,
                Instant.now()
        );

        log.info(
                "Revoked {} tokens for userId={}",
                updated,
                user.getId()
        );
    }

    @Override
    public void revokeSession(User user, String sessionId) {

        int updated = refreshTokenRepository.revokeByUserAndSession(
                user,
                sessionId,
                Instant.now()
        );

        log.info(
                "Revoked {} tokens for userId={}, sessionId={}",
                updated,
                user.getId(),
                sessionId
        );
    }

    @Transactional(readOnly = true)
    @Override
    public boolean validate(String rawToken, String sessionId, User user) {

        String hash = hashToken(rawToken);
        Instant now = Instant.now();

        RefreshToken token = refreshTokenRepository
                .findByTokenHashAndSessionId(hash, sessionId)
                .orElse(null);

        if (token == null) {
            throw new InvalidRefreshTokenException(rawToken, sessionId);
        }

        if (token.getExpiresAt().isBefore(now)) {
            throw new RefreshTokenExpiredException(sessionId);
        }

        if (token.isRevoked() || token.getUsedAt() != null) {
            throw new RefreshTokenReuseDetectedException(sessionId, user.getId());
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