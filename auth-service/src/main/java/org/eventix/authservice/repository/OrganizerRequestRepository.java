package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.OrganizerRequest;
import org.eventix.authservice.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizerRequestRepository extends JpaRepository<OrganizerRequest, Long> {
    List<OrganizerRequest> findAllByStatus(RequestStatus status);
}
