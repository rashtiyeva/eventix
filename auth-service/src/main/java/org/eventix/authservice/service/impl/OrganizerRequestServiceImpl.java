package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.model.entity.OrganizerRequest;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.RequestStatus;
import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.repository.OrganizerRequestRepository;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.service.OrganizerRequestService;
import org.eventix.authservice.service.RefreshTokenService;
import org.eventix.authservice.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizerRequestServiceImpl implements OrganizerRequestService {

    private final OrganizerRequestRepository repository;
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void createRequest(Long userId, String reason) {

        OrganizerRequest request = OrganizerRequest.builder()
                .userId(userId)
                .reason(reason)
                .status(RequestStatus.PENDING)
                .build();

        repository.save(request);
    }

    @Override
    @Transactional
    public void approveRequest(Long requestId) {

        OrganizerRequest request = repository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Organizer request not found: " + requestId));

        request.setStatus(RequestStatus.APPROVED);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + request.getUserId()));

        user.setRole(UserRole.ORGANIZER);

        userRepository.save(user);

        sessionService.revokeAll(user.getId());
        refreshTokenService.revokeAll(user.getId());
    }

    @Override
    @Transactional
    public void rejectRequest(Long requestId) {

        OrganizerRequest request = repository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Organizer request not found: " + requestId));

        request.setStatus(RequestStatus.REJECTED);
    }
}