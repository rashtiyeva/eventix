package org.eventix.authservice.mapper;

import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.model.enums.UserRole;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtMapper {

    public List<String> toClaimsRoles(Set<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(UserRole::name)
                .toList();
    }

    public Set<UserRole> fromClaimsRoles(Object rawRoles) {
        if (!(rawRoles instanceof List<?> list) || list.isEmpty()) {
            return Set.of();
        }

        return list.stream()
                .map(String::valueOf)
                .map(this::safeParseRole)
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Optional<UserRole> safeParseRole(String role) {
        try {
            return Optional.of(UserRole.valueOf(role));
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown role in JWT ignored: {}", role);
            return Optional.empty();
        }
    }
}