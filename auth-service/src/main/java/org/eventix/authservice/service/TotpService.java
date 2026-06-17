package org.eventix.authservice.service;

import org.springframework.stereotype.Service;

@Service
public interface TotpService {
    String generateSecret();
    String getQrUri(String secret, String email);
    boolean verifyCode(String secret, String code);
}