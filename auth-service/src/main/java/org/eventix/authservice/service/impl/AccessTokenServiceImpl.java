package org.eventix.authservice.service.impl;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.mapper.JwtMapper;
import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.security.JwtClaims;
import org.eventix.authservice.security.JwtProperties;
import org.eventix.authservice.service.AccessTokenService;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessTokenServiceImpl implements AccessTokenService {

    private final JwtProperties jwtProperties;
    private final PrivateKey signingKey;
    private final JwtMapper jwtMapper;

    @Override
    public String generateAccessToken(Long userId, Set<UserRole> roles){

        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.accessExpiration());

        String tokenId = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(tokenId)
                .subject(String.valueOf(userId))
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim(JwtClaims.ROLES, jwtMapper.toClaimsRoles(roles))
                .claim("type", "access")
                .signWith(signingKey, Jwts.SIG.RS256)
                .compact();
    }
}

