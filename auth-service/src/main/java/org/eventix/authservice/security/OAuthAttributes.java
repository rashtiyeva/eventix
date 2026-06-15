package org.eventix.authservice.security;

import org.eventix.authservice.model.enums.OAuthProvider;

public record OAuthAttributes(
        OAuthProvider provider,
        String providerUserId,
        String email,
        boolean emailVerified
) {}
