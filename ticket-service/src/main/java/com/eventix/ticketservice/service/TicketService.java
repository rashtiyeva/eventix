package com.eventix.ticketservice.service;

import com.eventix.ticketservice.model.dto.request.TicketPurchaseRequest;
import com.eventix.ticketservice.model.dto.response.TicketResponse;
import com.eventix.ticketservice.model.entity.Ticket;

import java.util.List;

public interface TicketService {

    TicketResponse purchase(Long eventId);

    void confirm(Long ticketId);

    void cancel(Long ticketId);

    void expire(Long ticketId);
}