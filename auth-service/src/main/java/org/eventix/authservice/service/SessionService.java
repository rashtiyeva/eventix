package org.eventix.authservice.service;

import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SessionService {

    Session create(User user, String ip, String userAgent);

    Session getActiveSession(String sessionId);

    void revoke(Session session);

    void revokeAll(Long userId);

    List<Session> getUserSessions(Long userId);

    Session getUserSession(Long userId, String sessionId);
}
