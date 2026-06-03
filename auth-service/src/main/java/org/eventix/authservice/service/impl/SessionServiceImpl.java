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

        return sessionRepository.findByIdAndStatus(
                session.getId(),
                SessionStatus.ACTIVE
        ).orElseThrow(() ->
                new SessionNotActiveException(session.getId())
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
}