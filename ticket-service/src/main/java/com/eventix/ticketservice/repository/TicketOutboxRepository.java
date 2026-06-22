package com.eventix.ticketservice.repository;

import com.eventix.ticketservice.model.entity.TicketOutboxEvent;
import com.eventix.ticketservice.model.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketOutboxRepository extends JpaRepository<TicketOutboxEvent, Long> {

    List<TicketOutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
