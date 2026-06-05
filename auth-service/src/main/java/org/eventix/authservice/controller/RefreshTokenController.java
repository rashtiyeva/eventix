package org.eventix.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eventix.authservice.model.dto.request.*;
import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.dto.response.RefreshTokenValidationResponse;
import org.eventix.authservice.security.UserPrincipal;
import org.eventix.authservice.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/refresh-token")
@RequiredArgsConstructor
@Validated
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RotateRefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
                refreshTokenService.refresh(request.refreshToken())
        );
    }

    @PostMapping("/revoke/all")
    public ResponseEntity<Void> revokeAll(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        refreshTokenService.revokeAll(principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revoke/session/{sessionId}")
    public ResponseEntity<Void> revokeSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        refreshTokenService.revokeSession(
                principal.getUserId(),
                sessionId
        );

        return ResponseEntity.noContent().build();
    }
}