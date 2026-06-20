package org.eventix.eventservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.eventservice.model.dto.PageDetailDto;
import org.eventix.eventservice.model.dto.request.EventCreateRequest;
import org.eventix.eventservice.model.dto.request.EventPatchRequest;
import org.eventix.eventservice.model.dto.request.EventUpdateRequest;
import org.eventix.eventservice.model.dto.response.EventPreviewResponse;
import org.eventix.eventservice.model.dto.response.EventResponse;
import org.eventix.eventservice.model.enums.EventStatus;
import org.eventix.eventservice.security.AuthUser;
import org.eventix.eventservice.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final AuthUser authUser;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request
    ) {
        log.debug("HTTP POST /events title={}", request.title());

        Long userId = authUser.getUserId();

        EventResponse response = eventService.createEvent(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(
            @PathVariable Long id
    ) {
        log.debug("HTTP GET /events/{}", id);

        EventResponse response = eventService.getEvent(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<EventPreviewResponse>> getEvents(
            @RequestParam EventStatus status,
            @RequestParam int page,
            @RequestParam int size
    ) {
        log.debug("HTTP GET /events status={} page={} size={}", status, page, size);

        Page<EventPreviewResponse> response = eventService.getEventsByStatus(
                status,
                new PageDetailDto(page, size)
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request
    ) {
        log.debug("HTTP PUT /events/{}", id);

        Long userId = authUser.getUserId();

        EventResponse response = eventService.updateEvent(id, userId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventResponse> patchEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventPatchRequest request
    ) {
        log.debug("HTTP PATCH /events/{}", id);

        Long userId = authUser.getUserId();

        EventResponse response = eventService.patchEvent(id, userId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable Long id
    ) {
        log.debug("HTTP PATCH /events/{}/publish", id);

        Long userId = authUser.getUserId();

        EventResponse response = eventService.publishEvent(id, userId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EventResponse> cancelEvent(
            @PathVariable Long id
    ) {
        log.debug("HTTP PATCH /events/{}/cancel", id);

        Long userId = authUser.getUserId();

        EventResponse response = eventService.cancelEvent(id, userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id
    ) {
        log.debug("HTTP DELETE /events/{}", id);

        Long userId = authUser.getUserId();

        eventService.deleteEvent(id, userId);

        return ResponseEntity.noContent().build();
    }
}