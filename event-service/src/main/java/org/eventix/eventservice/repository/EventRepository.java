package org.eventix.eventservice.repository;

import org.eventix.eventservice.model.entity.Event;
import org.eventix.eventservice.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByStatus(EventStatus status, Pageable pageable);

    List<Event> findAllByOrganizerId(Long organizerId);

    Optional<Event> findByIdAndStatus(Long id, EventStatus status);

    List<Event> findAllByStatusAndIdGreaterThan(
            EventStatus status,
            Long afterId,
            Pageable pageable
    );
}