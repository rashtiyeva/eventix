package org.eventix.authservice.service;

import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.security.JwtClaims;
import java.util.Set;

public interface AccessTokenService {
    String generateAccessToken(String userId, Set<UserRole> roles);

    JwtClaims extractClaims(String token);
}
