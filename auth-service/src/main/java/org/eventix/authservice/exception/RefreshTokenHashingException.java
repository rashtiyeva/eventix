package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.RefreshTokenException;

public class RefreshTokenHashingException extends RefreshTokenException {
  public RefreshTokenHashingException() {
    super("Refresh token hashing failed");
  }
}