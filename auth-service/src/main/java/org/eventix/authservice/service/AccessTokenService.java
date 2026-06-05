package org.eventix.authservice.service;

import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.security.JwtClaims;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public interface AccessTokenService {
    String generateAccessToken(String userId, Set<UserRole> roles);

    JwtClaims extractClaims(String token);
}
