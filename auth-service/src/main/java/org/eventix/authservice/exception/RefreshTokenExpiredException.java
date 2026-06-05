package org.eventix.authservice.exception;

import org.eventix.authservice.exception.base.RefreshTokenException;
import org.eventix.authservice.model.entity.Session;

public class RefreshTokenExpiredException extends RefreshTokenException {
  public RefreshTokenExpiredException() {
    super("Refresh token expired for session");
  }
}
