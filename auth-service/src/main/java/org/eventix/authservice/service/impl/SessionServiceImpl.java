package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.temporal.ChronoUnit;
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

        log.debug("Session create request userId={}, deviceKey={}, ip={}",
                user.getId(), deviceKey, ip);

        return sessionRepository
                .findActiveByUserIdAndDeviceKey(user.getId(), deviceKey)
                .map(session -> {
                    log.debug("Active session found for reuse sessionId={}, userId={}",
                            session.getId(), user.getId());
                    return touch(session, ip, userAgent);
                })
                .orElseGet(() -> {
                    log.info("Creating new session userId={}, deviceKey={}, ip={}",
                            user.getId(), deviceKey, ip);
                    return createNewSession(user, ip, userAgent, deviceKey);
                });
    }

    @Override
    public Session getSession(String sessionId) {

        log.debug("Fetching session sessionId={}", sessionId);

        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.warn("Session not found sessionId={}", sessionId);
                    return new SessionNotFoundException(sessionId);
                });
    }

    @Override
    @Transactional
    public void revoke(String sessionId) {

        log.debug("Revoke session request sessionId={}", sessionId);

        int updated = sessionRepository.updateStatusById(
                sessionId,
                SessionStatus.REVOKED,
                SessionStatus.ACTIVE
        );

        if (updated == 0) {
            log.debug("Session already revoked or not active: {}", sessionId);
        } else {
            log.info("Session revoked sessionId={}", sessionId);
        }
    }

    @Override
    @Transactional
    public void revokeAll(Long userId) {

        log.debug("Revoke all sessions request userId={}", userId);

        sessionRepository.updateStatusByUserId(
                userId,
                SessionStatus.REVOKED,
                SessionStatus.ACTIVE
        );

        log.info("All sessions revoked for userId={}", userId);
    }

    @Override
    public List<Session> getUserSessions(Long userId) {

        log.debug("Fetching active sessions userId={}", userId);

        List<Session> sessions = sessionRepository.findAllByUserIdAndStatus(
                userId,
                SessionStatus.ACTIVE
        );

        log.debug("Found {} active sessions for userId={}", sessions.size(), userId);

        return sessions;
    }

    @Override
    public Session getUserSession(Long userId, String sessionId) {

        log.debug("Fetching user session userId={}, sessionId={}", userId, sessionId);

        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> {
                    log.warn("User session not found userId={}, sessionId={}",
                            userId, sessionId);
                    return new SessionNotFoundException(sessionId);
                });
    }

    @Transactional
    @Override
    public int expireInactiveSessions() {

        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);

        int updated = sessionRepository.expireInactiveSessions(cutoff);

        log.info("Expired {} inactive sessions", updated);

        return updated;
    }

    @Transactional
    @Override
    public int deleteOldSessions() {

        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        int deleted = sessionRepository.deleteOldSessions(cutoff);

        log.info("Deleted {} old sessions", deleted);

        return deleted;
    }



    private Session createNewSession(User user, String ip, String userAgent, String deviceKey) {

        log.debug("Creating new session entity userId={}, deviceKey={}",
                user.getId(), deviceKey);

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

        Session saved = sessionRepository.save(session);

        log.info("New session created userId={}, sessionId={}, ip={}",
                user.getId(), saved.getId(), ip);

        return saved;
    }

    private Session touch(Session session, String ip, String userAgent) {

        log.debug("Touching session sessionId={}, userId={}",
                session.getId(), session.getUser().getId());

        session.setLastUsedAt(Instant.now());
        session.setIpAddress(ip);
        session.setUserAgent(userAgent);

        Session updated = sessionRepository.save(session);

        log.debug("Session updated sessionId={}, lastUsedAt={}",
                updated.getId(), updated.getLastUsedAt());

        return updated;
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
            log.error("Device key hashing failed", e);
            throw new RuntimeException(e);
        }
    }
}