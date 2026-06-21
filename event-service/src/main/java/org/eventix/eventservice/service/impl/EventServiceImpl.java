package org.eventix.eventservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.eventservice.exception.EventNotFoundException;
import org.eventix.eventservice.mapper.EventMapper;
import org.eventix.eventservice.model.dto.PageDetailDto;
import org.eventix.eventservice.model.dto.request.EventCreateRequest;
import org.eventix.eventservice.model.dto.request.EventPatchRequest;
import org.eventix.eventservice.model.dto.request.EventUpdateRequest;
import org.eventix.eventservice.model.dto.response.EventPreviewResponse;
import org.eventix.eventservice.model.dto.response.EventResponse;
import org.eventix.eventservice.model.entity.Event;
import org.eventix.eventservice.model.enums.EventStatus;
import org.eventix.eventservice.repository.EventRepository;
import org.eventix.eventservice.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponse createEvent(EventCreateRequest request, Long organizerId) {

        try {

            Event event = eventMapper.toEntity(request);

            event.setOrganizerId(organizerId);
            event.setStatus(EventStatus.DRAFT);

            if (event.getCapacity() == null || event.getCapacity() <= 0) {
                event.setCapacity(1);
            }

            Event saved = eventRepository.save(event);

            return eventMapper.toResponse(saved);

        } catch (Exception e) {
            log.error("Create event failed", e);
            throw e;
        }
    }

    @Override
    public Page<EventPreviewResponse> getEventsByStatus(EventStatus status, PageDetailDto pageDetail) {

        PageRequest pageable = getDefaultPageable(pageDetail.page(), pageDetail.size());

        Page<Event> events = eventRepository.findAllByStatus(status, pageable);

        return events.map(eventMapper::toPreview);
    }

    @Override
    public EventResponse getEvent(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        return eventMapper.toResponse(event);
    }

    @Override
    public EventResponse updateEvent(Long id, Long userId, EventUpdateRequest request) {

        Event event = getOwnedEvent(id, userId);

        if (event.getStatus() == EventStatus.PUBLISHED) {
            throw new IllegalStateException("Published event cannot be fully updated");
        }

        eventMapper.updateFromRequest(request, event);

        Event updated = eventRepository.save(event);

        return eventMapper.toResponse(updated);
    }

    @Override
    public EventResponse patchEvent(Long id, Long userId, EventPatchRequest request) {

        Event event = getOwnedEvent(id, userId);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled event cannot be modified");
        }

        eventMapper.patchFromRequest(request, event);

        Event updated = eventRepository.save(event);

        return eventMapper.toResponse(updated);
    }

    @Override
    public EventResponse publishEvent(Long id, Long userId) {

        Event event = getOwnedEvent(id, userId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT events can be published");
        }

        event.setStatus(EventStatus.PUBLISHED);

        Event updated = eventRepository.save(event);

        return eventMapper.toResponse(updated);
    }

    @Override
    public EventResponse cancelEvent(Long id, Long userId) {

        Event event = getOwnedEvent(id, userId);

        if (event.getStatus() == EventStatus.CANCELLED) {
            return eventMapper.toResponse(event);
        }

        event.setStatus(EventStatus.CANCELLED);

        Event updated = eventRepository.save(event);

        return eventMapper.toResponse(updated);
    }

    @Override
    public void deleteEvent(Long id, Long userId) {

        Event event = getOwnedEvent(id, userId);

        eventRepository.delete(event);
    }

    private Event getOwnedEvent(Long eventId, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOrganizerId().equals(userId)) {
            throw new AccessDeniedException("You are not the owner of this event");
        }

        return event;
    }

    private static PageRequest getDefaultPageable(int page, int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        return PageRequest.of(
                safePage,
                safeSize,
                Sort.by("createdAt").descending()
        );
    }

}
