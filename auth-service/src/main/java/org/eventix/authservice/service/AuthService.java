package org.eventix.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    @Transactional
    AuthResponse register(RegisterRequest registerRequest, HttpServletRequest httpRequest);

    AuthResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest);
}
