package org.eventix.authservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.service.RefreshTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "0 */30 * * * *")
    public void markExpiredTokens() {

        log.debug("Starting refresh token expiration job");

        refreshTokenService.markExpiredTokens();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteOldTokens() {

        log.debug("Starting refresh token cleanup job");

        refreshTokenService.deleteOldTokens();
    }
}