package org.eventix.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.eventix.authservice.mapper.SessionMapper;
import org.eventix.authservice.model.dto.request.CreateSessionRequest;
import org.eventix.authservice.model.dto.request.RevokeSessionRequest;
import org.eventix.authservice.model.dto.response.SessionResponse;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.security.UserPrincipal;
import org.eventix.authservice.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final SessionMapper sessionMapper;

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateSessionRequest request
    ) {
        Session session = sessionService.create(
                principal.user(),
                request.ip(),
                request.userAgent()
        );

        return ResponseEntity.ok(
                sessionMapper.toResponse(session)
        );
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getActiveSession(
            @PathVariable String sessionId
    ) {
        Session session = sessionService.getActiveSession(
                Session.builder().id(sessionId).build()
        );

        return ResponseEntity.ok(
                sessionMapper.toResponse(session)
        );
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(
            @RequestBody RevokeSessionRequest request
    ) {
        sessionService.revoke(
                Session.builder()
                        .id(request.sessionId())
                        .build()
        );

        return ResponseEntity.noContent().build();
    }
    @PostMapping("/revoke/all")
    public ResponseEntity<Void> revokeAll(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        sessionService.revokeAll(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}