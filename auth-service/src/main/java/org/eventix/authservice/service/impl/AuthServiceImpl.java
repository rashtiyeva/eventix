package org.eventix.authservice.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.EmailAlreadyExistsException;
import org.eventix.authservice.exception.InvalidCredentialsException;
import org.eventix.authservice.exception.InvalidTempTokenException;
import org.eventix.authservice.exception.SessionNotFoundException;
import org.eventix.authservice.mapper.AuthMapper;
import org.eventix.authservice.model.dto.TempLoginState;
import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.eventix.authservice.model.dto.response.LoginResponse;
import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.dto.response.TwoFaSetupResponse;
import org.eventix.authservice.model.entity.OAuth2Identity;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.SessionStatus;
import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.model.enums.UserStatus;
import org.eventix.authservice.repository.OAuth2IdentityRepository;
import org.eventix.authservice.repository.SessionRepository;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.security.OAuthAttributes;
import org.eventix.authservice.security.RequestContext;
import org.eventix.authservice.service.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
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
    private final OAuth2IdentityRepository identityRepository;
    private final TempTokenService tempTokenService;
    private final TotpService totpService;
    private final RateLimitService rateLimitService;
    private final CryptoService cryptoService;
    private final RecoveryCodeService recoveryCodeService;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {

        String ip = RequestContext.getIp();
        String userAgent = RequestContext.getUserAgent();

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
    public LoginResponse login(LoginRequest request) {

        String ip = RequestContext.getIp();
        String userAgent = RequestContext.getUserAgent();

        rateLimitService.checkLoginLimit(request.email());

        User user = userRepository.findByEmailAndStatus(
                        request.email(),
                        UserStatus.ACTIVE
                )
                .orElseThrow(() -> {
                    rateLimitService.recordLoginFail(request.email());
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            rateLimitService.recordLoginFail(request.email());
            throw new InvalidCredentialsException();
        }

        rateLimitService.resetLogin(request.email());

        if (user.isTwoFactorEnabled()) {

            String tempToken = tempTokenService.create(
                    user.getId(),
                    ip,
                    userAgent
            );

            return new LoginResponse(true, tempToken, null);
        }

        Session session = createSession(user, ip, userAgent);

        return new LoginResponse(
                false,
                null,
                issueAuthResponse(user, session)
        );
    }

    public TwoFaSetupResponse setup2fa(Long userId) {

        User user = userRepository.findById(userId).orElseThrow();

        String secret = totpService.generateSecret();

        user.setTotpSecret(cryptoService.encrypt(secret));
        userRepository.save(user);
        String qrUri = totpService.getQrUri(secret, user.getEmail());

        return new TwoFaSetupResponse(qrUri);
    }

    @Override
    @Transactional
    public AuthResponse verify2fa(String tempToken, String code) {

        TempLoginState state = validateTempTokenOrThrow(tempToken);

        String ip = RequestContext.getIp();
        String userAgent = RequestContext.getUserAgent();
        String deviceId = RequestContext.getDeviceId();

        Long userId = state.userId();

        if (!Objects.equals(state.deviceId(), deviceId)) {
            log.warn("Device mismatch userId={}", userId);
        }

        if (!Objects.equals(state.ip(), ip) || !Objects.equals(state.userAgent(), userAgent)) {
            log.info("Context change userId={}", userId);
        }

        rateLimitService.check2faLimit(userId);
        rateLimitService.checkRecoveryLimit(userId);

        User user = getActiveUserOrThrow(userId);

        String secret = decryptTotpSecretOrThrow(user);

        boolean totpValid = totpService.verifyCode(secret, code);

        boolean recoveryValid = false;
        if (!totpValid) {
            recoveryValid = recoveryCodeService.verifyRecoveryCode(userId, code);
        }

        if (!totpValid && !recoveryValid) {

            rateLimitService.record2faFail(userId);
            rateLimitService.recordRecoveryFail(userId);

            int attempts = state.attempts() + 1;

            if (attempts >= 5) {
                tempTokenService.invalidate(tempToken);
            } else {
                tempTokenService.update(tempToken,
                        new TempLoginState(
                                state.userId(),
                                state.deviceId(),
                                state.ip(),
                                state.userAgent(),
                                attempts,
                                state.createdAt()
                        ));
            }

            throw new InvalidCredentialsException();
        }

        rateLimitService.reset2fa(userId);
        rateLimitService.resetRecovery(userId);

        tempTokenService.invalidate(tempToken);

        Session session = createSession(user, ip, userAgent);

        return issueAuthResponse(user, session);
    }

    @Transactional
    @Override
    public AuthResponse loginOAuth(OAuthAttributes attr, HttpServletRequest request) {

        OAuth2Identity identity = identityRepository
                .findByProviderAndProviderUserId(
                        attr.provider(),
                        attr.providerUserId()
                )
                .orElse(null);

        User user;

        if (identity != null) {
            user = identity.getUser();
        }

        else {
            user = createOAuthUser(attr.email());

            linkIdentity(user, attr);
        }

        Session session = sessionService.create(
                user,
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        String accessToken = accessTokenService.generateAccessToken(
                user.getId(),
                Set.of(user.getRole())
        );

        RefreshTokenResponse refreshToken =
                refreshTokenService.createToken(user.getId(), session.getId());

        return new AuthResponse(
                accessToken,
                refreshToken.refreshToken(),
                authMapper.mapToUserResponse(user)
        );
    }

    private TempLoginState validateTempTokenOrThrow(String tempToken) {

        if (tempToken == null || tempToken.isBlank()) {
            throw new InvalidTempTokenException("tempToken is null/blank");
        }

        TempLoginState state = tempTokenService.validate(tempToken);

        if (state == null) {
            throw new InvalidTempTokenException("TempLoginState not found");
        }

        if (state.userId() == null) {
            throw new InvalidTempTokenException("TempLoginState userId is null");
        }

        return state;
    }

    private User getActiveUserOrThrow(Long userId) {

        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new InvalidTempTokenException("User not found or inactive: " + userId)
                );
    }

    private String decryptTotpSecretOrThrow(User user) {

        if (user.getTotpSecret() == null || user.getTotpSecret().isBlank()) {
            throw new IllegalStateException("2FA not initialized for userId=" + user.getId());
        }

        try {
            String secret = cryptoService.decrypt(user.getTotpSecret());

            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException("Decrypted TOTP secret is empty");
            }

            return secret;

        } catch (Exception ex) {
            log.error("TOTP decrypt failed userId={}", user.getId(), ex);
            throw new IllegalStateException("TOTP decrypt error");
        }
    }


    private User createOAuthUser(String email) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password(null)
                        .role(UserRole.BUYER)
                        .status(UserStatus.ACTIVE)
                        .build()
        );
    }

    private void linkIdentity(User user, OAuthAttributes attr) {
        OAuth2Identity identity = OAuth2Identity.builder()
                .user(user)
                .provider(attr.provider())
                .providerUserId(attr.providerUserId())
                .email(attr.email())
                .emailVerified(attr.emailVerified())
                .build();

        identityRepository.save(identity);
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
                user.getId(),
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