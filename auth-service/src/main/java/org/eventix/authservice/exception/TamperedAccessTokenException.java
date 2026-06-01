package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.AccessTokenException;

public class TamperedAccessTokenException extends AccessTokenException {

    public TamperedAccessTokenException() {
        super("Access token signature is invalid");
    }
}
