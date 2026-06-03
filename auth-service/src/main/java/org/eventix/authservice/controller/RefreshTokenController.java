package org.eventix.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eventix.authservice.model.dto.request.*;
import org.eventix.authservice.model.dto.response.RefreshTokenResponse;
import org.eventix.authservice.model.dto.response.RefreshTokenValidationResponse;
import org.eventix.authservice.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/refresh-token")
@RequiredArgsConstructor
@Validated
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/generate")
    public ResponseEntity<RefreshTokenResponse> createToken(
            @Valid @RequestBody CreateRefreshTokenRequest request
    ) {

        RefreshTokenResponse response = refreshTokenService.createToken(
                request.user(),
                request.session()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RotateRefreshTokenRequest request
    ) {

        RefreshTokenResponse response = refreshTokenService.refresh(
                request.refreshToken(),
                request.sessionId(),
                request.userId()
        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/validate")
    public ResponseEntity<RefreshTokenValidationResponse> validateToken(
            @Valid @RequestBody ValidateRefreshTokenRequest request
    ) {

        refreshTokenService.validate(
                request.refreshToken(),
                request.sessionId(),
                request.userId()
        );

        return ResponseEntity.ok(
                new RefreshTokenValidationResponse(true)
        );
    }

    @PostMapping("/revoke/all")
    public ResponseEntity<Void> revokeAll(
            @Valid @RequestBody RevokeUserRefreshTokenRequest request
    ) {

        refreshTokenService.revokeAll(request.user().getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revoke/session")
    public ResponseEntity<Void> revokeSession(
            @Valid @RequestBody RevokeSessionRequest request
    ) {

        refreshTokenService.revokeSession(
                request.userId(),
                request.sessionId()
        );

        return ResponseEntity.noContent().build();
    }
}