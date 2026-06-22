package com.eventix.ticketservice.controller;

import com.eventix.ticketservice.model.dto.response.TicketResponse;
import com.eventix.ticketservice.security.AuthUser;
import com.eventix.ticketservice.service.TicketService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tickets")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TicketController {

    private final TicketService ticketService;
    private final AuthUser authUser;

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TicketResponse> purchase(
            @RequestParam @NotNull Long eventId
    ) {
        log.debug("HTTP POST /tickets/purchase eventId={}", eventId);

        Long userId = authUser.getUserId();

        TicketResponse response =
                ticketService.purchase(userId, eventId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{ticketId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public ResponseEntity<Void> confirm(
            @PathVariable @NotNull Long ticketId
    ) {
        log.debug("HTTP POST /tickets/{}/confirm", ticketId);

        ticketService.confirm(ticketId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{ticketId}/cancel")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Void> cancel(
            @PathVariable @NotNull Long ticketId
    ) {
        log.debug("HTTP POST /tickets/{}/cancel", ticketId);

        ticketService.cancel(ticketId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{ticketId}/expire")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> expire(
            @PathVariable @NotNull Long ticketId
    ) {
        log.debug("HTTP POST /tickets/{}/expire", ticketId);

        ticketService.expire(ticketId);

        return ResponseEntity.noContent().build();
    }
}