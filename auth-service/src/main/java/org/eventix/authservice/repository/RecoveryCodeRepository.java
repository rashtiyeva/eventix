package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.RecoveryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface RecoveryCodeRepository extends JpaRepository<RecoveryCode, Long> {

    List<RecoveryCode> findByUserIdAndUsedFalse(Long userId);

    void deleteByUserId(Long userId);
}