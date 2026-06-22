package com.eventix.ticketservice.kafka;

import com.eventix.ticketservice.event.TicketRejectedEvent;
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
public class TicketRejectedConsumer {

    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "TICKET_REJECTED", groupId = "ticket-service")
    @Transactional
    public void handle(String message) {

        try {

            TicketRejectedEvent event =
                    objectMapper.readValue(message, TicketRejectedEvent.class);

            log.info(
                    "TICKET REJECTION START sagaId={}, ticketId={}, eventId={}, reason={}",
                    event.sagaId(),
                    event.ticketId(),
                    event.eventId(),
                    event.reason()
            );

            Ticket ticket = ticketRepository.findById(event.ticketId())
                    .orElseThrow(() ->
                            new RuntimeException("Ticket not found: " + event.ticketId()));

            // idempotency guard
            if (ticket.getStatus() == TicketStatus.CANCELLED) {

                log.info(
                        "TICKET ALREADY CANCELLED sagaId={}, ticketId={}",
                        event.sagaId(),
                        ticket.getId()
                );

                return;
            }

            // business rule validation
            if (ticket.getStatus() == TicketStatus.CONFIRMED) {

                log.warn(
                        "TICKET REJECTION IGNORED sagaId={}, ticketId={}, currentStatus={}",
                        event.sagaId(),
                        ticket.getId(),
                        ticket.getStatus()
                );

                return;
            }

            ticket.setStatus(TicketStatus.CANCELLED);

            log.warn(
                    "TICKET CANCELLED sagaId={}, ticketId={}, reason={}",
                    event.sagaId(),
                    ticket.getId(),
                    event.reason()
            );

        } catch (Exception e) {

            log.error(
                    "TICKET REJECTION FAILED",
                    e
            );

            throw new RuntimeException(e);
        }
    }
}