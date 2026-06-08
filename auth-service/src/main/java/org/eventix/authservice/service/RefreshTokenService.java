package org.eventix.authservice.service;

import org.eventix.authservice.model.dto.response.RefreshTokenResponse;

public interface RefreshTokenService {

    RefreshTokenResponse createToken(Long userId, String sessionId);

    RefreshTokenResponse refresh(String rawToken);

    void revokeAll(Long userId);

    void revokeSession(Long userId, String sessionId);

    int markExpiredTokens();


    int deleteOldTokens();
}