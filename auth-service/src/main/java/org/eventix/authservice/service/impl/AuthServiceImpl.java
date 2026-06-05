package org.eventix.authservice.service.impl;

import jakarta.servlet.http.HttpServletRequest;
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

import java.util.Optional;
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

        validateEmail(request.email());

        User user = createUser(request);
        User savedUser = userRepository.save(user);

        Session session = createSession(savedUser, ip, userAgent);

        return issueAuthResponse(savedUser, session);
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest request, String ip, String userAgent) {

        User user = userRepository.findByEmailAndStatus(
                        request.email(),
                        UserStatus.ACTIVE
                )
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        Session session = createSession(user, ip, userAgent);

        return issueAuthResponse(user, session);
    }

    @Transactional
    @Override
    public void logout(String sessionId, Long userId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Session does not belong to user");
        }

        session.setStatus(SessionStatus.REVOKED);
        sessionRepository.save(session);

        refreshTokenService.revokeSession(
                session.getUser().getId(),
                session.getId()
        );

        log.info("User {} logged out from session {}", userId, sessionId);
    }

    @Transactional
    @Override
    public void logoutAll(Long userId) {

        sessionService.revokeAll(userId);
        refreshTokenService.revokeAll(userId);

        log.info("User {} logged out from all devices", userId);
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
            throw new EmailAlreadyExistsException();
        }
    }

    private AuthResponse issueAuthResponse(User user, Session session) {

        String accessToken = accessTokenService.generateAccessToken(
                user.getId().toString(),
                Set.of(user.getRole())
        );

        RefreshTokenResponse refreshToken =
                refreshTokenService.createToken(user, session);

        return new AuthResponse(
                accessToken,
                refreshToken.refreshToken(),
                authMapper.mapToUserResponse(user)
        );
    }

    private Session createSession(User user, String ip, String userAgent) {
        return sessionService.create(user, ip, userAgent);
    }

}
