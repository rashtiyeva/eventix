package org.eventix.eventservice.service;

import org.eventix.eventservice.model.dto.PageDetailDto;
import org.eventix.eventservice.model.dto.request.EventCreateRequest;
import org.eventix.eventservice.model.dto.request.EventPatchRequest;
import org.eventix.eventservice.model.dto.request.EventUpdateRequest;
import org.eventix.eventservice.model.dto.response.EventPreviewResponse;
import org.eventix.eventservice.model.dto.response.EventResponse;
import org.eventix.eventservice.model.enums.EventStatus;
import org.springframework.data.domain.Page;

public interface EventService {

    EventResponse createEvent(EventCreateRequest request, Long organizerId);

    Page<EventPreviewResponse> getEventsByStatus(EventStatus status, PageDetailDto pageDetail);

    EventResponse getEvent(Long id);

    EventResponse updateEvent(Long id, Long userId, EventUpdateRequest request);

    EventResponse patchEvent(Long id, Long userId, EventPatchRequest request);

    EventResponse publishEvent(Long id, Long userId);

    EventResponse cancelEvent(Long id, Long userId);

    void deleteEvent(Long id, Long userId);
}
