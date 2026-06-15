package org.eventix.authservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eventix.authservice.mapper.OAuthAttributesMapper;
import org.eventix.authservice.model.dto.response.AuthResponse;
import org.eventix.authservice.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthAttributesMapper attributesMapper;
    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken token =
                (OAuth2AuthenticationToken) authentication;

        OAuth2User oauthUser =
                (OAuth2User) authentication.getPrincipal();

        OAuthAttributes attributes = attributesMapper.map(
                token.getAuthorizedClientRegistrationId(),
                oauthUser.getAttributes()
        );

        AuthResponse authResponse =
                authService.loginOAuth(attributes, request);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                objectMapper.writeValueAsString(authResponse)
        );
    }
}