package org.eventix.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.eventix.authservice.model.dto.response.LoginResponse;
import org.eventix.authservice.model.dto.response.TwoFaSetupResponse;
import org.eventix.authservice.security.OAuthAttributes;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    TwoFaSetupResponse setup2fa(Long userId);

    AuthResponse verify2fa(String tempToken, String code);

    AuthResponse loginOAuth(OAuthAttributes attr, HttpServletRequest request);

    void logout(String sessionId, Long userId);

    void logoutAll(Long userId);
}
