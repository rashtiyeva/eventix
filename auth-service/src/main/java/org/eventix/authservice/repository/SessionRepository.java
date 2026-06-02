package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface SessionRepository extends JpaRepository<Session, String> {

    @Modifying
    @Query("""
            update Session s
            set s.status = :status
            where s.user.id = :userId
            """)
    void updateStatusByUserId(
            @Param("userId") Long userId,
            @Param("status") SessionStatus status
    );

}