package org.eventix.eventservice.exception.base;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BadRequestException extends RuntimeException {

    public static final String MESSAGE = "BAD_REQUEST";

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String resource, Object... args) {
        super(buildMessage(resource, args));
    }

    private static String buildMessage(String resource, Object... args) {
        String details = (args == null || args.length == 0)
                ? "invalid"
                : Arrays.stream(args)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        return resource + ": " + details;
    }
}