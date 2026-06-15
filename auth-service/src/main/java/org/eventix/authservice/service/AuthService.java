package org.eventix.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.eventix.authservice.security.OAuthAttributes;

public interface AuthService {

    AuthResponse register(RegisterRequest request, String ip, String userAgent);

    AuthResponse login(LoginRequest request, String ip, String userAgent);

    AuthResponse loginOAuth(OAuthAttributes attr, HttpServletRequest request);

    void logout(String sessionId, Long userId);

    void logoutAll(Long userId);
}
