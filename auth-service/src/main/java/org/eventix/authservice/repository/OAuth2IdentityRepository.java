package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.OAuth2Identity;
import org.eventix.authservice.model.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OAuth2IdentityRepository extends JpaRepository<OAuth2Identity, Long> {

    Optional<OAuth2Identity> findByProviderAndProviderUserId(
            OAuthProvider provider,
            String providerUserId
    );

    List<OAuth2Identity> findAllByUserId(Long userId);

    boolean existsByProviderAndProviderUserId(
            OAuthProvider provider,
            String providerUserId
    );
}