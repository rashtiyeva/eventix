package org.eventix.authservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.service.SessionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupScheduler {

    private final SessionService sessionService;

    @Scheduled(cron = "0 0 * * * *")
    public void expireInactiveSessions() {

        log.debug("Starting session expiration job");

        int updated = sessionService.expireInactiveSessions();

        log.info("Session expiration completed updated={}", updated);
    }


    @Scheduled(cron = "0 30 2 * * *")
    public void deleteOldSessions() {

        log.debug("Starting session cleanup job");

        int deleted = sessionService.deleteOldSessions();

        log.info("Session cleanup completed deleted={}", deleted);
    }
}
