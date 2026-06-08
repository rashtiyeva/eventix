package org.eventix.authservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.event.UserDeletedEvent;
import org.eventix.authservice.service.RefreshTokenService;
import org.eventix.authservice.service.SessionService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupListener {

    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;

    @Async
    @EventListener
    public void handle(UserDeletedEvent event) {

        Long userId = event.userId();

        log.debug("Cleaning up user id={}", userId);

        refreshTokenService.revokeAll(userId);
        sessionService.revokeAll(userId);

        log.info("Cleanup completed for user id={}", userId);
    }
}