package org.eventix.authservice.service;

import org.eventix.authservice.model.dto.TempLoginState;

public interface TempTokenService {

    String create(Long userId, String ip, String userAgent);

    TempLoginState validate(String token);

    void update(String token, TempLoginState state);

    void invalidate(String token);
}