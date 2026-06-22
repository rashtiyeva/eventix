package com.eventix.ticketservice.kafka;

import com.eventix.ticketservice.repository.TicketOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.eventix.ticketservice.model.entity.TicketOutboxEvent;
import com.eventix.ticketservice.model.enums.OutboxStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final TicketOutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publish() {

        List<TicketOutboxEvent> events =
                repository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);

        for (TicketOutboxEvent event : events) {

            try {

                log.info(
                        "OUTBOX PROCESSING sagaId={}, outboxId={}, eventType={}, aggregateId={}",
                        event.getSagaId(),
                        event.getId(),
                        event.getEventType(),
                        event.getAggregateId()
                );

                event.setStatus(OutboxStatus.PROCESSING);
                repository.save(event);

                kafkaTemplate.send(
                        event.getEventType(),
                        event.getPayload()
                );

                event.setStatus(OutboxStatus.PUBLISHED);
                repository.save(event);

                log.info(
                        "OUTBOX PUBLISHED sagaId={}, outboxId={}, eventType={}, aggregateId={}",
                        event.getSagaId(),
                        event.getId(),
                        event.getEventType(),
                        event.getAggregateId()
                );

            } catch (Exception e) {

                log.error(
                        "OUTBOX FAILED sagaId={}, outboxId={}, eventType={}, aggregateId={}",
                        event.getSagaId(),
                        event.getId(),
                        event.getEventType(),
                        event.getAggregateId(),
                        e
                );

                event.setStatus(OutboxStatus.FAILED);
                repository.save(event);
            }
        }
    }
}