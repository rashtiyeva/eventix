package com.eventix.ticketservice.service.impl;

import com.eventix.ticketservice.event.TicketReservedEvent;
import com.eventix.ticketservice.exception.TicketAlreadyExistsException;
import com.eventix.ticketservice.exception.TicketNotFoundException;
import com.eventix.ticketservice.exception.TicketOutboxSerializationException;
import com.eventix.ticketservice.model.entity.TicketOutboxEvent;
import com.eventix.ticketservice.model.enums.OutboxStatus;
import com.eventix.ticketservice.repository.TicketOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.eventix.ticketservice.mapper.TicketMapper;
import com.eventix.ticketservice.model.dto.response.TicketResponse;
import com.eventix.ticketservice.model.entity.Ticket;
import com.eventix.ticketservice.model.enums.TicketStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eventix.ticketservice.repository.TicketRepository;
import com.eventix.ticketservice.service.TicketService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketOutboxRepository outboxRepository;
    private final TicketMapper ticketMapper;
    private final ObjectMapper objectMapper;

    @Override
    public TicketResponse purchase(Long userId, Long eventId) {

        log.info("Purchasing ticket: userId={}, eventId={}", userId, eventId);

        try {
            Ticket ticket = Ticket.builder()
                    .userId(userId)
                    .eventId(eventId)
                    .status(TicketStatus.RESERVED)
                    .build();

            ticket = ticketRepository.save(ticket);

            log.info(
                    "TICKET CREATED sagaId={}, ticketId={}, userId={}, eventId={}",
                    ticket.getSagaId(),
                    ticket.getId(),
                    userId,
                    eventId
            );

            TicketReservedEvent event = new TicketReservedEvent(
                    ticket.getId(),
                    userId,
                    eventId,
                    ticket.getSagaId()
            );

            saveOutbox(event, ticket.getId());

            log.info(
                    "TICKET RESERVED sagaId={}, ticketId={}, eventId={}",
                    ticket.getSagaId(),
                    ticket.getId(),
                    ticket.getEventId()
            );

            return ticketMapper.toResponse(ticket);

        } catch (DataIntegrityViolationException e) {

            log.warn(
                    "TICKET DUPLICATE sagaId=UNKNOWN userId={}, eventId={}",
                    userId,
                    eventId
            );

            throw new TicketAlreadyExistsException(userId, eventId);
        }
    }

    @Override
    public void confirm(Long ticketId) {

        Ticket ticket = get(ticketId);

        log.info(
                "TICKET CONFIRMING sagaId={}, ticketId={}",
                ticket.getSagaId(),
                ticket.getId()
        );

        ticket.setStatus(TicketStatus.CONFIRMED);

        log.info(
                "TICKET CONFIRMED sagaId={}, ticketId={}",
                ticket.getSagaId(),
                ticket.getId()
        );
    }

    @Override
    public void cancel(Long ticketId) {

        Ticket ticket = get(ticketId);

        log.info(
                "TICKET CANCELLING sagaId={}, ticketId={}",
                ticket.getSagaId(),
                ticket.getId()
        );

        ticket.setStatus(TicketStatus.CANCELLED);

        log.warn(
                "TICKET CANCELLED sagaId={}, ticketId={}",
                ticket.getSagaId(),
                ticket.getId()
        );
    }

    @Override
    public void expire(Long ticketId) {

        Ticket ticket = get(ticketId);

        log.info(
                "TICKET EXPIRING sagaId={}, ticketId={}",
                ticket.getSagaId(),
                ticket.getId()
        );

        ticket.setStatus(TicketStatus.EXPIRED);

        ticketRepository.save(ticket);

        log.warn(
                "TICKET EXPIRED sagaId={}, ticketId={}",
                ticket.getSagaId(),
                ticket.getId()
        );
    }

    private Ticket get(Long id) {

        return ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ticket not found: id={}", id);
                    return new TicketNotFoundException(id);
                });
    }

    private void saveOutbox(Object event, Long aggregateId) {

        try {
            TicketOutboxEvent outbox = TicketOutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .eventType(event.getClass().getSimpleName())
                    .payload(objectMapper.writeValueAsString(event))
                    .status(OutboxStatus.NEW)
                    .createdAt(Instant.now())
                    .build();

            outboxRepository.save(outbox);

            log.info(
                    "OUTBOX CREATED type={}, aggregateId={}",
                    event.getClass().getSimpleName(),
                    aggregateId
            );

        } catch (Exception e) {

            log.error(
                    "Failed to serialize outbox event: aggregateId={}",
                    aggregateId,
                    e
            );

            throw new TicketOutboxSerializationException(
                    "Failed to serialize outbox event for aggregateId=" + aggregateId,
                    e
            );
        }
    }
}
