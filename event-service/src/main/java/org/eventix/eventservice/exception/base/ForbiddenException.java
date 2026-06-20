package org.eventix.eventservice.exception.base;

import java.util.Arrays;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String resource, Object... args) {
        super(resource + " forbidden: " + String.join(", ",
                Arrays.stream(args).map(String::valueOf).toList()));
    }
}