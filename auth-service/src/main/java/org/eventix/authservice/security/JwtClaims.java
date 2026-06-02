package org.eventix.authservice.security;

import org.eventix.authservice.model.enums.UserRole;
import java.util.Set;

public record JwtClaims(
        String userId,
        Set<UserRole> roles
) {
    public static final String ROLES = "roles";
}