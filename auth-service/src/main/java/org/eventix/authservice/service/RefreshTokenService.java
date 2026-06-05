package org.eventix.authservice.service;

import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.RefreshToken;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenService {

    RefreshTokenResponse createToken(User user, Session session);

    RefreshTokenResponse refresh(String rawToken);

    void revokeAll(Long userId);

    void revokeSession(Long userId, String sessionId);

}