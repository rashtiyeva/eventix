package org.eventix.authservice.service;


public interface RecoveryCodeService {

    boolean verifyRecoveryCode(Long userId, String code);

}