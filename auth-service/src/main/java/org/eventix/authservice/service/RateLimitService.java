package org.eventix.authservice.service;

public interface RateLimitService {

    void checkLoginLimit(String email);

    void recordLoginFail(String email);

    void resetLogin(String email);

    void check2faLimit(Long userId);

    void record2faFail(Long userId);

    void reset2fa(Long userId);

    void checkRecoveryLimit(Long userId);

    void recordRecoveryFail(Long userId);

    void resetRecovery(Long userId);
}