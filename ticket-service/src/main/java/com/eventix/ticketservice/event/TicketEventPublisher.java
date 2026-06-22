package com.eventix.ticketservice.event;

import lombok.RequiredArgsConstructor;
import com.eventix.ticketservice.model.entity.Ticket;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void ticketReserved(Ticket t) {
        publisher.publishEvent(new TicketReservedEvent(
                t.getId(),
                t.getUserId(),
                t.getEventId(),
                t.getSagaId()
        ));
    }

    public void ticketConfirmed(Ticket t) {}

    public void ticketCancelled(Ticket t) {}

    public void ticketExpired(Ticket t) {}
}