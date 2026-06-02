package org.eventix.authservice.service;

import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface SessionService {

    Session create(User user, String ip, String userAgent);

    Session getActiveSession(String sessionId);

    void revoke(String sessionId);

    void revokeAll(Long userId);
}
