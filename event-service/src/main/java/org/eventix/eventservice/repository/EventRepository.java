package org.eventix.eventservice.repository;

import jakarta.persistence.LockModeType;
import org.eventix.eventservice.model.entity.Event;
import org.eventix.eventservice.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {


    Page<Event> findAllByStatusAndDeletedAtIsNull(
            EventStatus status,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select e
    from Event e
    where e.id = :id
""")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    Optional<Event> findByIdAndDeletedAtIsNull(Long id);
}