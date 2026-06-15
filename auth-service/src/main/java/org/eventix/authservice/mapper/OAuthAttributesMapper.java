package org.eventix.authservice.mapper;

import org.eventix.authservice.exception.UnsupportedOAuthProviderException;
import org.eventix.authservice.model.enums.OAuthProvider;
import org.eventix.authservice.security.OAuthAttributes;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuthAttributesMapper {

    public OAuthAttributes map(String registrationId, Map<String, Object> attributes) {

        OAuthProvider provider = OAuthProvider.valueOf(
                registrationId.toUpperCase()
        );

        return switch (provider) {

            case GOOGLE -> mapGoogle(attributes);

            case GITHUB -> mapGitHub(attributes);

            default -> throw new UnsupportedOAuthProviderException(registrationId);
        };
    }

    private OAuthAttributes mapGoogle(Map<String, Object> attributes) {

        return new OAuthAttributes(
                OAuthProvider.GOOGLE,
                (String) attributes.get("sub"),
                (String) attributes.get("email"),
                Boolean.TRUE.equals(attributes.get("email_verified"))
        );
    }

    private OAuthAttributes mapGitHub(Map<String, Object> attributes) {

        return new OAuthAttributes(
                OAuthProvider.GITHUB,
                String.valueOf(attributes.get("id")),
                (String) attributes.get("email"),
                true
        );
    }
}