package com.eventix.ticketservice.client;

import com.eventix.ticketservice.model.dto.EventDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/events/{id}")
    EventDto getEvent(@PathVariable Long id);
}