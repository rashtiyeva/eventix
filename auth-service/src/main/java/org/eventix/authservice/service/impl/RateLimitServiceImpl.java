package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.TooMany2faAttemptsException;
import org.eventix.authservice.exception.TooManyLoginAttemptsException;
import org.eventix.authservice.security.RequestContext;
import org.eventix.authservice.service.RateLimitService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private static final int LOGIN_LIMIT = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(10);
    private static final int USER_2FA_LIMIT = 15;
    private static final int IP_2FA_LIMIT = 100;
    private static final Duration USER_LOCK_TIME = Duration.ofMinutes(30);
    private static final int RECOVERY_LIMIT = 5;


    @Override
    public void checkLoginLimit(String email) {

        Long emailCount = getCount(loginEmailKey(email));
        if (emailCount >= LOGIN_LIMIT) {
            throw new TooManyLoginAttemptsException();
        }

        String ip = RequestContext.getIp();
        Long ipCount = getCount(loginIpKey(ip));

        if (ipCount >= LOGIN_LIMIT * 3) {
            throw new TooManyLoginAttemptsException();
        }
    }

    @Override
    public void recordLoginFail(String email) {
        increment(loginEmailKey(email));
        increment(loginIpKey(RequestContext.getIp()));
    }

    @Override
    public void resetLogin(String email) {
        redisTemplate.delete(loginEmailKey(email));
    }


    @Override
    public void check2faLimit(Long userId) {

        String ip = RequestContext.getIp();

        if (redisTemplate.hasKey(userLockKey(userId))) {
            throw new TooMany2faAttemptsException();
        }

        Long userCount = getCount(user2faKey(userId));
        if (userCount >= USER_2FA_LIMIT) {

            redisTemplate.opsForValue().set(
                    userLockKey(userId),
                    "locked",
                    USER_LOCK_TIME
            );

            throw new TooMany2faAttemptsException();
        }

        Long ipCount = getCount(ip2faKey(ip));
        if (ipCount >= IP_2FA_LIMIT) {
            throw new TooMany2faAttemptsException();
        }
    }

    @Override
    public void record2faFail(Long userId) {

        String ip = RequestContext.getIp();

        increment(user2faKey(userId));
        increment(ip2faKey(ip));
    }

    @Override
    public void reset2fa(Long userId) {

        String ip = RequestContext.getIp();

        redisTemplate.delete(user2faKey(userId));
        redisTemplate.delete(ip2faKey(ip));
        redisTemplate.delete(userLockKey(userId));
    }

    @Override
    public void checkRecoveryLimit(Long userId) {

        Long count = getCount(recoveryKey(userId));

        if (count >= RECOVERY_LIMIT) {
            throw new TooManyLoginAttemptsException();
        }
    }

    @Override
    public void recordRecoveryFail(Long userId) {
        increment(recoveryKey(userId));
    }

    @Override
    public void resetRecovery(Long userId) {
        redisTemplate.delete(recoveryKey(userId));
    }

    private String recoveryKey(Long userId) {
        return "recovery:user:" + userId;
    }

    private void increment(String key) {

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, LOGIN_WINDOW);
        }

    }

    private Long getCount(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    private String loginEmailKey(String email) {
        return "login:email:" + email;
    }

    private String loginIpKey(String ip) {
        return "login:ip:" + ip;
    }

    private String user2faKey(Long userId) {
        return "2fa:user:" + userId;
    }

    private String ip2faKey(String ip) {
        return "2fa:ip:" + ip;
    }

    private String userLockKey(Long userId) {
        return "2fa:lock:user:" + userId;
    }
}