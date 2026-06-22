package org.eventix.eventservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.eventservice.event.TicketConfirmedEvent;
import org.eventix.eventservice.event.TicketRejectedEvent;
import org.eventix.eventservice.event.TicketReservedEvent;
import org.eventix.eventservice.model.entity.Event;
import org.eventix.eventservice.model.entity.ProcessedEvent;
import org.eventix.eventservice.repository.EventRepository;
import org.eventix.eventservice.repository.ProcessedEventRepository;
import org.eventix.eventservice.service.EventReservationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketEventConsumer {

    private final EventReservationService reservationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "TICKET_RESERVED",
            groupId = "event-service"
    )
    public void handle(String message) {

        try {

            TicketReservedEvent event =
                    objectMapper.readValue(message, TicketReservedEvent.class);

            log.info(
                    "EVENT PROCESSING START sagaId={}, ticketId={}, eventId={}",
                    event.sagaId(),
                    event.ticketId(),
                    event.eventId()
            );

            reservationService.processReservation(event);

        } catch (Exception e) {

            log.error(
                    "EVENT PROCESSING FAILED",
                    e
            );

            throw new RuntimeException(e);
        }
    }
}
