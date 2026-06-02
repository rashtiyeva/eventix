package org.eventix.authservice.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.EmailAlreadyExistsException;
import org.eventix.authservice.exception.InvalidCredentialsException;
import org.eventix.authservice.mapper.AuthMapper;
import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.service.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest){

        validateEmail(request.email());

        User user = createUser(request);
        User savedUser = userRepository.save(user);

        Session session = createSession(savedUser, httpRequest);

        return buildAuthResponse(savedUser, session.getId());
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        Session session = createSession(user, httpRequest);

        return buildAuthResponse(user, session.getId());
    }

    private User createUser(RegisterRequest request) {

        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.BUYER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void validateEmail(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }
    }

    private AuthResponse buildAuthResponse(User user, String sessionId) {

        String accessToken = accessTokenService.generateAccessToken(
                user.getId().toString(),
                Set.of(user.getRole())
        );

        RefreshTokenResponse refreshTokenResponse =
                refreshTokenService.createToken(user, sessionId);

        String refreshToken = refreshTokenResponse.refreshToken();

        return new AuthResponse(
                accessToken,
                refreshToken,
                authMapper.mapToUserResponse(user)
        );
    }

    private Session createSession(User user, HttpServletRequest request) {

        String ip = getClientIp(request);
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent"))
                .orElse("unknown");

        return sessionService.create(user, ip, userAgent);
    }

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
