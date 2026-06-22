package org.eventix.eventservice.service;

import org.eventix.eventservice.event.TicketReservedEvent;

public interface EventReservationService {

    void processReservation(TicketReservedEvent event);
}
