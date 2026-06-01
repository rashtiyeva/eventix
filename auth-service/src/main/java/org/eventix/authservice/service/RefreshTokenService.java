package org.eventix.authservice.service;

import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface RefreshTokenService {

    RefreshTokenResponse createToken(User user, String sessionId);

    @Transactional
    RefreshTokenResponse refresh(String rawToken, String sessionId, User user);

    void revokeAll(User user);

    void revokeSession(User user, String sessionId);


    @Transactional(readOnly = true)
    boolean validate(String rawToken, String sessionId, User user);
}