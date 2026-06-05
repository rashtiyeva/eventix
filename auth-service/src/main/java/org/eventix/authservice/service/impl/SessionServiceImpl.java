package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.SessionNotActiveException;
import org.eventix.authservice.exception.SessionNotFoundException;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.SessionStatus;
import org.eventix.authservice.repository.SessionRepository;
import org.eventix.authservice.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public Session create(User user, String ip, String userAgent) {

        String deviceKey = buildDeviceKey(userAgent);

        return sessionRepository
                .findActiveByUserIdAndDeviceKey(user.getId(), deviceKey)
                .map(session -> touch(session, ip, userAgent))
                .orElseGet(() -> createNewSession(user, ip, userAgent, deviceKey));
    }


    @Override
    public Session getActiveSession(String sessionId) {

        return sessionRepository.findByIdAndStatus(
                sessionId,
                SessionStatus.ACTIVE
        ).orElseThrow(() ->
                new SessionNotActiveException(sessionId)
        );
    }

    @Override
    public void revoke(Session session) {

        int updated = sessionRepository.updateStatusById(
                session.getId(),
                SessionStatus.REVOKED,
                SessionStatus.ACTIVE
        );

        if (updated == 0) {
            throw new SessionNotFoundException(session.getId());
        }
    }

    @Override
    @Transactional
    public void revokeAll(Long userId) {

        sessionRepository.updateStatusByUserId(
                userId,
                SessionStatus.REVOKED,
                SessionStatus.ACTIVE
        );
    }

    @Override
    public List<Session> getUserSessions(Long userId) {
        return sessionRepository.findAllByUserIdAndStatus(
                userId,
                SessionStatus.ACTIVE
        );
    }

    @Override
    public Session getUserSession(Long userId, String sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    private Session createNewSession(User user, String ip, String userAgent, String deviceKey) {

        Session session = Session.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .status(SessionStatus.ACTIVE)
                .createdAt(Instant.now())
                .lastUsedAt(Instant.now())
                .ipAddress(ip)
                .userAgent(userAgent)
                .deviceKey(deviceKey)
                .build();

        return sessionRepository.save(session);
    }
    private Session touch(Session session, String ip, String userAgent) {

        session.setLastUsedAt(Instant.now());
        session.setIpAddress(ip);
        session.setUserAgent(userAgent);

        return sessionRepository.save(session);
    }

    private String buildDeviceKey(String userAgent) {

        return hash(normalizeUserAgent(userAgent));
    }

    private String normalizeUserAgent(String userAgent) {
        return userAgent == null ? "unknown" : userAgent.trim();
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}