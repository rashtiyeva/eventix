package org.eventix.authservice.service.impl;


import lombok.RequiredArgsConstructor;
import org.eventix.authservice.model.entity.RecoveryCode;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.repository.RecoveryCodeRepository;
import org.eventix.authservice.service.RecoveryCodeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecoveryCodeServiceImpl implements RecoveryCodeService {

    private final RecoveryCodeRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean verifyRecoveryCode(Long userId, String code) {

        List<RecoveryCode> codes =
                repository.findByUserIdAndUsedFalse(userId);

        for (RecoveryCode rc : codes) {

            if (passwordEncoder.matches(code, rc.getCodeHash())) {

                rc.setUsed(true);
                repository.save(rc);

                return true;
            }
        }

        return false;
    }


    private String generateCode() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
