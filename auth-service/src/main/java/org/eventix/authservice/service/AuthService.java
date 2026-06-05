package org.eventix.authservice.service;

import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    AuthResponse register(RegisterRequest request, String ip, String userAgent);

    AuthResponse login(LoginRequest request, String ip, String userAgent);

    void logout(String sessionId, Long userId);

    void logoutAll(Long userId);
}
