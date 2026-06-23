package org.eventix.eventservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.eventservice.event.TicketConfirmedEvent;
import org.eventix.eventservice.event.TicketRejectedEvent;
import org.eventix.eventservice.event.TicketReservedEvent;
import org.eventix.eventservice.exception.EventNotFoundException;
import org.eventix.eventservice.model.entity.Event;
import org.eventix.eventservice.model.entity.ProcessedEvent;
import org.eventix.eventservice.model.enums.EventStatus;
import org.eventix.eventservice.repository.EventRepository;
import org.eventix.eventservice.repository.ProcessedEventRepository;
import org.eventix.eventservice.service.EventReservationService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventReservationServiceImpl implements EventReservationService {

    private final EventRepository eventRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void processReservation(TicketReservedEvent event) {

        String key = event.ticketId() + "-" + event.eventId();

        if (processedEventRepository.existsById(key)) {

            log.info(
                    "EVENT ALREADY PROCESSED sagaId={}, ticketId={}, eventId={}",
                    event.sagaId(),
                    event.ticketId(),
                    event.eventId()
            );

            return;
        }

        Event entity = eventRepository.findByIdForUpdate(event.eventId())
                .orElseThrow(() ->
                        new EventNotFoundException(event.eventId()));

        if (entity.getStatus() != EventStatus.PUBLISHED) {

            publishRejected(event, "EVENT_NOT_PUBLISHED");

            processedEventRepository.save(
                    new ProcessedEvent(key, Instant.now())
            );

            log.warn(
                    "EVENT NOT PUBLISHED sagaId={}, eventId={}, status={}",
                    event.sagaId(),
                    entity.getId(),
                    entity.getStatus()
            );

            return;
        }

        if (entity.getReserved() >= entity.getCapacity()) {

            publishRejected(event, "EVENT_FULL");

            processedEventRepository.save(
                    new ProcessedEvent(key, Instant.now())
            );

            log.warn(
                    "EVENT FULL sagaId={}, eventId={}",
                    event.sagaId(),
                    entity.getId()
            );

            return;
        }

        entity.setReserved(entity.getReserved() + 1);

        eventRepository.save(entity);

        publishConfirmed(event);

        processedEventRepository.save(
                new ProcessedEvent(key, Instant.now())
        );

        log.info(
                "EVENT RESERVED sagaId={}, eventId={}, reserved={}, capacity={}",
                event.sagaId(),
                entity.getId(),
                entity.getReserved(),
                entity.getCapacity()
        );
    }

    private void publishConfirmed(TicketReservedEvent event) {

        try {

            TicketConfirmedEvent confirmed =
                    new TicketConfirmedEvent(
                            event.ticketId(),
                            event.userId(),
                            event.email(),
                            event.sagaId()
                    );

            kafkaTemplate.send(
                    "TICKET_CONFIRMED",
                    objectMapper.writeValueAsString(confirmed)
            );

            log.info(
                    "EVENT CONFIRMATION SENT sagaId={}, ticketId={}, eventId={}",
                    event.sagaId(),
                    event.ticketId(),
                    event.eventId()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish confirmation event", e);
        }
    }

    private void publishRejected(TicketReservedEvent event, String reason) {

        try {

            TicketRejectedEvent rejected =
                    new TicketRejectedEvent(
                            event.ticketId(),
                            event.userId(),
                            event.eventId(),
                            event.email(),
                            reason,
                            event.sagaId()
                    );

            kafkaTemplate.send(
                    "TICKET_REJECTED",
                    objectMapper.writeValueAsString(rejected)
            );

            log.warn(
                    "EVENT REJECTION SENT sagaId={}, ticketId={}, eventId={}, reason={}",
                    event.sagaId(),
                    event.ticketId(),
                    event.eventId(),
                    reason
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish rejection event", e);
        }
    }
}