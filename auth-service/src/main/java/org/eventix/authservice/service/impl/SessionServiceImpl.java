package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.eventix.authservice.exception.SessionNotActiveException;
import org.eventix.authservice.exception.SessionNotFoundException;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.SessionStatus;
import org.eventix.authservice.repository.SessionRepository;
import org.eventix.authservice.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                .user(user)
                .status(SessionStatus.ACTIVE)
                .createdAt(Instant.now())
                .lastUsedAt(Instant.now())
                .ipAddress(ip)
                .userAgent(userAgent)
                .build();

        return sessionRepository.save(session);
    }

    @Override
    public Session getActiveSession(Session session) {

        Session fresh = sessionRepository.findById(session.getId())
                .orElseThrow(() -> new SessionNotFoundException(session.getId()));

        if (fresh.getStatus() != SessionStatus.ACTIVE) {
            throw new SessionNotActiveException(session.getId());
        }

        return fresh;
    }

    @Override
    public void revoke(Session session) {

        Session fresh = sessionRepository.findById(session.getId())
                .orElseThrow(() -> new SessionNotFoundException(session.getId()));

        fresh.setStatus(SessionStatus.REVOKED);

        sessionRepository.save(fresh);
    }

    @Override
    @Transactional
    public void revokeAll(Long userId) {
        sessionRepository.updateStatusByUserId(userId, SessionStatus.REVOKED);
    }
}