package org.eventix.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eventix.authservice.model.dto.request.GenerateAccessTokenRequest;
import org.eventix.authservice.model.dto.request.ValidateAccessTokenRequest;
import org.eventix.authservice.model.dto.response.AccessTokenResponse;
import org.eventix.authservice.security.JwtClaims;
import org.eventix.authservice.service.AccessTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/access-token")
@RequiredArgsConstructor
@Validated
public class AccessTokenController {

    private final AccessTokenService accessTokenService;

    @PostMapping("/generate")
    public ResponseEntity<AccessTokenResponse> generateToken(
            @Valid @RequestBody GenerateAccessTokenRequest request
    ) {

        String token = accessTokenService.generateAccessToken(
                request.userId(),
                request.roles()
        );

        return ResponseEntity.ok(
                new AccessTokenResponse(token)
        );
    }
}