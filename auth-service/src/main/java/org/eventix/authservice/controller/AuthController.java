package org.eventix.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.request.TwoFactorRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.eventix.authservice.model.dto.response.LoginResponse;
import org.eventix.authservice.model.dto.response.TwoFaSetupResponse;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.security.OAuthAttributes;
import org.eventix.authservice.security.UserPrincipal;
import org.eventix.authservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.debug("HTTP POST /register email={}", request.email());

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.debug("HTTP POST /login email={}", request.email());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<TwoFaSetupResponse> setup2fa(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                authService.setup2fa(principal.getUserId())
        );
    }


    @PostMapping("/2fa/verify")
    public ResponseEntity<AuthResponse> verify2fa(
            @Valid @RequestBody TwoFactorRequest request
    ) {
        log.debug("HTTP POST /2fa/verify tempToken={}", request.tempToken());

        AuthResponse response = authService.verify2fa(
                request.tempToken(),
                request.code()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/oauth/login")
    public ResponseEntity<AuthResponse> oauthLogin(
            @RequestBody OAuthAttributes request,
            HttpServletRequest httpRequest
    ) {
        log.debug("HTTP POST /oauth/login provider={}", request.provider());

        AuthResponse response = authService.loginOAuth(request, httpRequest);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestParam String sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        authService.logout(sessionId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout/all")
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        authService.logoutAll(principal.getUserId());

        return ResponseEntity.noContent().build();
    }
}