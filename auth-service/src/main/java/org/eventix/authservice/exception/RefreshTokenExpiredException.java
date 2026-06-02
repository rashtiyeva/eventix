package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.RefreshTokenException;

public class RefreshTokenExpiredException extends RefreshTokenException {
  public RefreshTokenExpiredException(String sessionId) {
    super("Refresh token expired for session: " + sessionId);
  }
}
