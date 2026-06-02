package org.eventix.authservice.service;

import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface RefreshTokenService {

    RefreshTokenResponse createToken(User user, Session session);

    @Transactional
    RefreshTokenResponse refresh(String rawToken, Session session, User user);

    void revokeAll(Long userId);

    void revokeSession(Long userId, String sessionId);

    @Transactional(readOnly = true)
    boolean validate(String rawToken, Session session, User user);
}