package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.RefreshTokenException;

public class RefreshTokenHashingException extends RefreshTokenException {
  public RefreshTokenHashingException(Throwable cause) {
    super("Refresh token hashing failed: " + cause.getMessage());
  }
}