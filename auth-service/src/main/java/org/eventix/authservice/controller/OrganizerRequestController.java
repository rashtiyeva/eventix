package org.eventix.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.eventix.authservice.model.dto.OrganizerRequestCreateDto;
import org.eventix.authservice.service.OrganizerRequestService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/organizer-requests")
@RequiredArgsConstructor
public class OrganizerRequestController {

    private final OrganizerRequestService service;

    @PostMapping
    public void createRequest(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody OrganizerRequestCreateDto dto
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        service.createRequest(userId, dto.reason());
    }

    @PatchMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        service.approveRequest(id);
    }

    @PatchMapping("/{id}/reject")
    public void reject(@PathVariable Long id) {
        service.rejectRequest(id);
    }
}