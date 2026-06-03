package org.eventix.authservice.service;

import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface RefreshTokenService {

    RefreshTokenResponse createToken(User user, Session session);

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3
    )
    RefreshTokenResponse refresh(
            String rawToken,
            String sessionId,
            Long userId
    );

    void revokeAll(Long userId);

    void revokeSession(Long userId, String sessionId);

    @Transactional(readOnly = true)
    void validate(String rawToken, String sessionId, Long userId);
}