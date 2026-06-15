package org.eventix.authservice.exception;

public class UnsupportedOAuthProviderException extends RuntimeException {

    public UnsupportedOAuthProviderException(String provider) {
        super("Unsupported OAuth provider: " + provider);
    }
}