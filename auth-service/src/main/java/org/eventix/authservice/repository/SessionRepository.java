package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findAllByUserId(Long userId);

    Optional<Session> findByIdAndStatus(String id, SessionStatus status);
}