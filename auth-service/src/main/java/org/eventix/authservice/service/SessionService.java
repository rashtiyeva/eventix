package org.eventix.authservice.service;

import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SessionService {

    Session create(User user, String ip, String userAgent);

    Session getSession(String sessionId);

    void revoke(String sessionId);

    void revokeAll(Long userId);

    List<Session> getUserSessions(Long userId);

    Session getUserSession(Long userId, String sessionId);

    int expireInactiveSessions();

    int deleteOldSessions();
}
