package com.eventix.ticketservice.scheduler;

import com.eventix.ticketservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import com.eventix.ticketservice.model.entity.Ticket;
import com.eventix.ticketservice.model.enums.TicketStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.eventix.ticketservice.repository.TicketRepository;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketExpirationScheduler {

    private final TicketRepository repository;
    private final TicketService ticketService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireTickets() {

        List<Long> expiredTicketIds = repository
                .findAllByStatusAndExpiresAtBefore(
                        TicketStatus.RESERVED,
                        Instant.now()
                )
                .stream()
                .map(Ticket::getId)
                .toList();

        for (Long ticketId : expiredTicketIds) {
            ticketService.expire(ticketId);
        }
    }
}