package org.eventix.authservice.service.impl;


import lombok.RequiredArgsConstructor;
import org.eventix.authservice.exception.InvalidTempTokenException;
import org.eventix.authservice.model.dto.TempLoginState;
import org.eventix.authservice.security.RequestContext;
import org.eventix.authservice.service.TempTokenService;
import org.eventix.authservice.util.JsonUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TempTokenServiceImpl implements TempTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JsonUtils jsonUtils;

    private static final Duration TTL = Duration.ofMinutes(10);

    @Override
    public String create(Long userId, String ip, String userAgent) {

        String token = UUID.randomUUID().toString();

        String deviceId = RequestContext.getDeviceId();

           if (deviceId == null || deviceId.isBlank()) {
            deviceId = "unknown";
        }

        TempLoginState state = new TempLoginState(
                userId,
                deviceId,
                ip,
                userAgent,
                0,
                Instant.now()
        );

        String key = key(token);

        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key,
                jsonUtils.toJson(state),
                TTL
        );

        if (Boolean.FALSE.equals(success)) {
            throw new IllegalStateException("Temp token collision");
        }

        return token;
    }

    @Override
    public TempLoginState validate(String token) {

        String key = key(token);

        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            throw new InvalidTempTokenException();
        }

        return jsonUtils.fromJson(json, TempLoginState.class);
    }

    @Override
    public void update(String token, TempLoginState state) {

        String key = key(token);

        redisTemplate.opsForValue().set(
                key,
                jsonUtils.toJson(state),
                TTL
        );
    }

    @Override
    public void invalidate(String token) {
        redisTemplate.delete(key(token));
    }

    private String key(String token) {
        return "temp:" + token;
    }
}