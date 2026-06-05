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

import java.util.List;

@RestController
@RequestMapping("/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final SessionMapper sessionMapper;

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Session session = sessionService.getUserSession(
                principal.getUserId(),
                sessionId
        );

        return ResponseEntity.ok(sessionMapper.toResponse(session));
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getUserSessions(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                sessionService.getUserSessions(principal.getUserId())
                        .stream()
                        .map(sessionMapper::toResponse)
                        .toList()
        );
    }
}