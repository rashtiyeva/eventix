package org.eventix.authservice.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.AccessTokenAuthenticationException;
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

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
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

        } catch (io.jsonwebtoken.ExpiredJwtException ex) {

            log.debug("Access token expired");
            throw new ExpiredAccessTokenException();

        } catch (io.jsonwebtoken.JwtException ex) {

            log.debug("Invalid access token");
            throw new InvalidAccessTokenException();
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