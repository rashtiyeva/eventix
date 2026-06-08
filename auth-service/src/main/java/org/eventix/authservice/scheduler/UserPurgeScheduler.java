package org.eventix.authservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserPurgeScheduler {

    private final UserService userService;

    @Scheduled(cron = "0 0 2 * * *")
    public void purgeDeletedUsers() {

        log.debug("Starting user purge job");

        int deleted = userService.purgeDeletedUsers();

        log.info("User purge completed deleted={}", deleted);
    }
}
