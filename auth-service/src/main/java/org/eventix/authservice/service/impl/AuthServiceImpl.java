package org.eventix.authservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.EmailAlreadyExistsException;
import org.eventix.authservice.exception.InvalidCredentialsException;
import org.eventix.authservice.exception.SessionNotFoundException;
import org.eventix.authservice.mapper.AuthMapper;
import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.SessionStatus;
import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.model.enums.UserStatus;
import org.eventix.authservice.repository.SessionRepository;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.service.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;
    private final SessionRepository sessionRepository;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request, String ip, String userAgent) {

        log.debug("Register attempt for email={}, ip={}, userAgent={}",
                request.email(), ip, userAgent);

        validateEmail(request.email());

        User user = createUser(request);
        User savedUser = userRepository.save(user);

        log.info("User registered successfully userId={}, email={}",
                savedUser.getId(), savedUser.getEmail());

        Session session = createSession(savedUser, ip, userAgent);

        log.debug("Session created for new user userId={}, sessionId={}",
                savedUser.getId(), session.getId());

        return issueAuthResponse(savedUser, session);
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest request, String ip, String userAgent) {

        log.debug("Login attempt for email={}, ip={}", request.email(), ip);

        User user = userRepository.findByEmailAndStatus(
                        request.email(),
                        UserStatus.ACTIVE
                )
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found or inactive email={}", request.email());
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed - invalid password userId={}, email={}",
                    user.getId(), user.getEmail());
            throw new InvalidCredentialsException();
        }

        log.debug("User authenticated successfully userId={}", user.getId());

        Session session = createSession(user, ip, userAgent);

        log.info("Login successful userId={}, sessionId={}",
                user.getId(), session.getId());

        return issueAuthResponse(user, session);
    }

    @Transactional
    @Override
    public void logout(String sessionId, Long userId) {

        log.debug("Logout request userId={}, sessionId={}", userId, sessionId);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.warn("Logout failed - session not found sessionId={}", sessionId);
                    return new SessionNotFoundException(sessionId);
                });

        if (!session.getUser().getId().equals(userId)) {
            log.warn("Logout denied - session ownership mismatch userId={}, sessionId={}",
                    userId, sessionId);
            throw new AccessDeniedException("Session does not belong to user");
        }

        session.setStatus(SessionStatus.REVOKED);
        sessionRepository.save(session);

        refreshTokenService.revokeSession(
                session.getUser().getId(),
                session.getId()
        );

        log.info("User logged out successfully userId={}, sessionId={}",
                userId, sessionId);
    }

    @Transactional
    @Override
    public void logoutAll(Long userId) {

        log.debug("Logout all sessions request userId={}", userId);

        sessionService.revokeAll(userId);
        refreshTokenService.revokeAll(userId);

        log.info("All sessions revoked for userId={}", userId);
    }

    private User createUser(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private void validateEmail(String email) {

        if (userRepository.existsByEmailAndStatus(email, UserStatus.ACTIVE)) {
            log.warn("Registration failed - email already exists email={}", email);
            throw new EmailAlreadyExistsException();
        }

        log.debug("Email validation passed email={}", email);
    }

    private AuthResponse issueAuthResponse(User user, Session session) {

        log.debug("Issuing auth response userId={}, sessionId={}",
                user.getId(), session.getId());

        String accessToken = accessTokenService.generateAccessToken(
                user.getId().toString(),
                Set.of(user.getRole())
        );

        RefreshTokenResponse refreshToken =
                refreshTokenService.createToken(
                        user.getId(),
                        session.getId()
                );

        log.info("Auth tokens issued userId={}, sessionId={}",
                user.getId(), session.getId());

        return new AuthResponse(
                accessToken,
                refreshToken.refreshToken(),
                authMapper.mapToUserResponse(user)
        );
    }

    private Session createSession(User user, String ip, String userAgent) {

        log.debug("Creating session userId={}, ip={}, userAgent={}",
                user.getId(), ip, userAgent);

        return sessionService.create(user, ip, userAgent);
    }
}