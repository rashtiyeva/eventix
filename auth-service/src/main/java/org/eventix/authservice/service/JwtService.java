package org.eventix.authservice.service;

import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.security.JwtClaims;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface JwtService {
    String generateAccessToken(String userId, Set<UserRole> roles);

    boolean isTokenValid(String token);

    JwtClaims extractClaims(String token);
}
