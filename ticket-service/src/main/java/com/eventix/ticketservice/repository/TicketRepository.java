package com.eventix.ticketservice.repository;

import com.eventix.ticketservice.model.entity.Ticket;
import com.eventix.ticketservice.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByUserId(Long userId);

    List<Ticket> findAllByEventId(Long eventId);

    Optional<Ticket> findByUserIdAndEventId(Long userId, Long eventId);

    List<Ticket> findAllByStatusAndExpiresAtBefore(
            TicketStatus status,
            Instant time
    );
}