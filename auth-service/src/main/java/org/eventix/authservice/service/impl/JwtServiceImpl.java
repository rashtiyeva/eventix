package org.eventix.authservice.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.JwtAuthenticationException;
import org.eventix.authservice.mapper.JwtMapper;
import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.security.JwtClaims;
import org.eventix.authservice.security.JwtProperties;
import org.eventix.authservice.service.JwtService;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;
    private final JwtMapper jwtMapper;

    @Override
    public String generateAccessToken(String userId, Set<UserRole> roles) {

        Instant now = Instant.now();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(
                                        jwtProperties.accessExpirationMinutes(),
                                        ChronoUnit.MINUTES
                                )
                        )
                )
                .claim(JwtClaims.ROLES, jwtMapper.toClaimsRoles(roles))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public JwtClaims extractClaims(String token) {

        try {
            Claims claims = parseClaims(token);

            Set<UserRole> roles = jwtMapper.fromClaimsRoles(
                    claims.get(JwtClaims.ROLES)
            );

            return new JwtClaims(
                    claims.getSubject(),
                    roles
            );

        } catch (JwtException ex) {
            log.debug("Invalid JWT while extracting claims");
            throw new JwtAuthenticationException("Invalid JWT token");
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;

        } catch (JwtException ex) {
            log.debug("JWT validation failed");
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.issuer())
                .clockSkewSeconds(15)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}