package com.eventix.ticketservice.security;


import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokenValidator
        implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        String type = jwt.getClaimAsString("type");

        if ("access".equals(type)) {
            return OAuth2TokenValidatorResult.success();
        }

        OAuth2Error error =
                new OAuth2Error(
                        "invalid_token",
                        "Access token required",
                        null
                );

        return OAuth2TokenValidatorResult.failure(error);
    }
}
