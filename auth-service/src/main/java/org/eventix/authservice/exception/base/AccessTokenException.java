package org.eventix.authservice.exception.base;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class AccessTokenException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "ACCESS_TOKEN_EXCEPTION";

    public AccessTokenException(String message) {
        super(message);
    }

    public AccessTokenException() {
        super(DEFAULT_MESSAGE);
    }

    public AccessTokenException(String reason, Object... args) {
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
