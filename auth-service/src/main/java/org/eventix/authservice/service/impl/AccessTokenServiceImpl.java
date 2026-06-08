package org.eventix.authservice.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.ExpiredAccessTokenException;
import org.eventix.authservice.exception.InvalidAccessTokenException;
import org.eventix.authservice.mapper.JwtMapper;
import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.security.JwtClaims;
import org.eventix.authservice.security.JwtProperties;
import org.eventix.authservice.service.AccessTokenService;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessTokenServiceImpl implements AccessTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;
    private final JwtMapper jwtMapper;

    @Override
    public String generateAccessToken(String userId, Set<UserRole> roles) {

        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.accessExpiration());

        log.debug("Generating access token for userId={}, roles={}, expiresAt={}",
                userId, roles, expiration);

        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim(JwtClaims.ROLES, jwtMapper.toClaimsRoles(roles))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        log.info("Access token generated successfully for userId={}, tokenId={}",
                userId,
                extractTokenIdSafely(token));

        return token;
    }

    @Override
    public JwtClaims extractClaims(String token) {

        log.debug("Extracting claims from access token");

        try {
            Claims claims = parseClaims(token);

            String userId = claims.getSubject();

            Set<UserRole> roles = jwtMapper.fromClaimsRoles(
                    claims.get(JwtClaims.ROLES)
            );

            log.info("Access token successfully parsed for userId={}, roles={}",
                    userId, roles);

            return new JwtClaims(
                    userId,
                    roles
            );

        } catch (io.jsonwebtoken.ExpiredJwtException ex) {

            log.warn("Access token expired: {}", ex.getMessage());
            throw new ExpiredAccessTokenException();

        } catch (io.jsonwebtoken.JwtException ex) {

            log.warn("Invalid access token: {}", ex.getMessage());
            throw new InvalidAccessTokenException();

        } catch (Exception ex) {

            log.error("Unexpected error while extracting access token claims", ex);
            throw new InvalidAccessTokenException();
        }
    }

    private Claims parseClaims(String token) {

        log.debug("Parsing JWT claims");

        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.issuer())
                .clockSkewSeconds(15)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String extractTokenIdSafely(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return String.valueOf(claims.getId());
        } catch (Exception e) {
            return "unknown";
        }
    }
}