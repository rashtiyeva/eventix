package org.eventix.authservice.service;

import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.security.JwtClaims;
import java.util.Set;

public interface AccessTokenService {
    String generateAccessToken(Long userId, Set<UserRole> roles);
}
