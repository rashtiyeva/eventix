package org.eventix.eventservice.exception.base;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NotFoundException extends RuntimeException {

    public static final String MESSAGE = "NOT_FOUND_EXCEPTION";

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
        super(MESSAGE);
    }

    public NotFoundException(String resource, Object... args) {
        super(buildMessage(resource, args));
    }

    private static String buildMessage(String resource, Object... args) {
        String details = (args == null || args.length == 0)
                ? "unknown"
                : Arrays.stream(args)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        return resource + " not found with: " + details;
    }
}