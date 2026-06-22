package com.eventix.ticketservice.mapper;

import com.eventix.ticketservice.model.dto.response.TicketResponse;
import com.eventix.ticketservice.model.entity.Ticket;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    TicketResponse toResponse(Ticket ticket);

    List<TicketResponse> toResponseList(List<Ticket> tickets);
}