package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {

    Optional<Session> findByIdAndStatus(String id, SessionStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Session s
        set s.status = :status
        where s.id = :id
          and s.status = :currentStatus
        """)
    int updateStatusById(
            @Param("id") String id,
            @Param("status") SessionStatus status,
            @Param("currentStatus") SessionStatus currentStatus
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Session s
        set s.status = :status
        where s.user.id = :userId
          and s.status = :currentStatus
        """)
    void updateStatusByUserId(
            @Param("userId") Long userId,
            @Param("status") SessionStatus status,
            @Param("currentStatus") SessionStatus currentStatus
    );
}