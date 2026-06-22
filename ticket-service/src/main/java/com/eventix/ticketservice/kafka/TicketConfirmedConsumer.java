package com.eventix.ticketservice.kafka;

import com.eventix.ticketservice.event.TicketConfirmedEvent;
import com.eventix.ticketservice.model.entity.Ticket;
import com.eventix.ticketservice.model.enums.TicketStatus;
import com.eventix.ticketservice.repository.TicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketConfirmedConsumer {

    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "TICKET_CONFIRMED", groupId = "ticket-service")
    @Transactional
    public void handle(String message) {

        try {

            TicketConfirmedEvent event =
                    objectMapper.readValue(message, TicketConfirmedEvent.class);

            log.info(
                    "TICKET CONFIRMATION START sagaId={}, ticketId={}, eventId={}",
                    event.sagaId(),
                    event.ticketId(),
                    event.eventId()
            );

            Ticket ticket = ticketRepository.findById(event.ticketId())
                    .orElseThrow(() ->
                            new RuntimeException("Ticket not found: " + event.ticketId()));

            // idempotency guard
            if (ticket.getStatus() == TicketStatus.CONFIRMED) {

                log.info(
                        "TICKET ALREADY CONFIRMED sagaId={}, ticketId={}",
                        event.sagaId(),
                        ticket.getId()
                );

                return;
            }

            // state transition validation
            if (ticket.getStatus() != TicketStatus.RESERVED) {

                log.warn(
                        "INVALID TICKET TRANSITION sagaId={}, ticketId={}, currentStatus={}",
                        event.sagaId(),
                        ticket.getId(),
                        ticket.getStatus()
                );

                return;
            }

            ticket.setStatus(TicketStatus.CONFIRMED);

            log.info(
                    "TICKET CONFIRMED sagaId={}, ticketId={}, eventId={}",
                    event.sagaId(),
                    ticket.getId(),
                    event.eventId()
            );

        } catch (Exception e) {

            log.error(
                    "TICKET CONFIRMATION FAILED",
                    e
            );

            throw new RuntimeException(e);
        }
    }
}