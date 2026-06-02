package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.SessionStatus;
import org.eventix.authservice.repository.SessionRepository;
import org.eventix.authservice.service.SessionService;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    @Override
    public Session create(User user, String ip, String userAgent) {

        Session session = Session.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .status(SessionStatus.ACTIVE)
                .createdAt(Instant.now())
                .lastUsedAt(Instant.now())
                .ipAddress(ip)
                .userAgent(userAgent)
                .build();

        return sessionRepository.save(session);
    }

    @Override
    public Session getActiveSession(String sessionId) {

        return sessionRepository.findById(sessionId)
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                .orElseThrow();
    }

    @Override
    public void revoke(String sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow();

        session.setStatus(SessionStatus.REVOKED);
        sessionRepository.save(session);
    }

    @Override
    public void revokeAll(Long userId) {

        sessionRepository.findAllByUserId(userId)
                .forEach(s -> s.setStatus(SessionStatus.REVOKED));

        sessionRepository.flush();
    }
}