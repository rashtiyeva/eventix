package org.eventix.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    @Transactional
    AuthResponse register(RegisterRequest registerRequest, HttpServletRequest httpRequest);

    AuthResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest);

    @Transactional
    void logout(String sessionId, Long userId);

    @Transactional
    void logoutAll(Long userId);
}
