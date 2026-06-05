package org.eventix.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eventix.authservice.model.dto.request.LoginRequest;
import org.eventix.authservice.model.dto.request.RegisterRequest;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.security.UserPrincipal;
import org.eventix.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                authService.register(
                        request,
                        httpRequest.getRemoteAddr(),
                        httpRequest.getHeader("User-Agent")
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                authService.login(
                        request,
                        httpRequest.getRemoteAddr(),
                        httpRequest.getHeader("User-Agent")
                )
        );
    }

    @PostMapping("/logout/{sessionId}")
    public ResponseEntity<Void> logout(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        authService.logout(sessionId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        authService.logoutAll(principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    }