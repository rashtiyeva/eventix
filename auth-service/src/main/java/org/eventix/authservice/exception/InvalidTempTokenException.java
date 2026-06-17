package org.eventix.authservice.exception;

public class InvalidTempTokenException extends RuntimeException {

    public InvalidTempTokenException() {
        super("Invalid or expired temporary token");
    }

    public InvalidTempTokenException(String message) {
        super(message);
    }
}