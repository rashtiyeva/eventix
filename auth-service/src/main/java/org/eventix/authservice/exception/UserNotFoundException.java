package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}
