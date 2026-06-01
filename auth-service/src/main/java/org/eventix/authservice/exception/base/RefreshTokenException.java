package org.eventix.authservice.exception.base;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class RefreshTokenException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "REFRESH_TOKEN_EXCEPTION";

    public RefreshTokenException(String message) {
        super(message);
    }

    public RefreshTokenException() {
        super(DEFAULT_MESSAGE);
    }

    public RefreshTokenException(String reason, Object... args) {
        super(buildMessage(reason, args));
    }

    private static String buildMessage(String reason, Object... args) {

        String details = (args == null || args.length == 0)
                ? "unknown"
                : Arrays.stream(args)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        return reason + " : " + details;
    }
}
