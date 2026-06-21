package org.eventix.authservice.service;


public interface OrganizerRequestService {

    void createRequest(Long userId, String reason);

    void approveRequest(Long requestId);

    void rejectRequest(Long requestId);
}
