package org.eventix.eventservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.eventservice.exception.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;


    @Override
    public EventResponse createEvent(EventCreateRequest request, Long organizerId) {

        log.info("Creating event organizerId={}", organizerId);

        Event event = eventMapper.toEntity(request);

        event.setOrganizerId(organizerId);
        event.setStatus(EventStatus.DRAFT);

        if (event.getCapacity() == null || event.getCapacity() <= 0) {
            event.setCapacity(1);
        }

        Event saved = eventRepository.save(event);

        log.info("Event created id={}, organizerId={}", saved.getId(), organizerId);

        return eventMapper.toResponse(saved);
    }


    @Override
    public Page<EventPreviewResponse> getEventsByStatus(EventStatus status, PageDetailDto pageDetail) {

        PageRequest pageable = getDefaultPageable(pageDetail.page(), pageDetail.size());

        return eventRepository
                .findAllByStatusAndDeletedAtIsNull(status, pageable)
                .map(eventMapper::toPreview);
    }

    @Override
    public EventResponse getEvent(Long id) {

        Event event = findActiveEvent(id);

        return eventMapper.toResponse(event);
    }


    @Override
    public EventResponse updateEvent(Long id, Long userId, EventUpdateRequest request) {

        Event event = getOwnedActiveEvent(id, userId);

        ensureUpdatable(event);

        eventMapper.updateFromRequest(request, event);

        Event saved = eventRepository.save(event);

        log.info("Event updated id={}, userId={}", id, userId);

        return eventMapper.toResponse(saved);
    }

    @Override
    public EventResponse patchEvent(Long id, Long userId, EventPatchRequest request) {

        Event event = getOwnedActiveEvent(id, userId);

        ensureUpdatable(event);

        eventMapper.patchFromRequest(request, event);

        Event saved = eventRepository.save(event);

        log.info("Event patched id={}, userId={}", id, userId);

        return eventMapper.toResponse(saved);
    }


    @Override
    public EventResponse publishEvent(Long id, Long userId) {

        Event event = getOwnedActiveEvent(id, userId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventPublishNotAllowedException("Only DRAFT events can be published");
        }

        event.setStatus(EventStatus.PUBLISHED);

        Event saved = eventRepository.save(event);

        log.info("Event published id={}, userId={}", id, userId);

        return eventMapper.toResponse(saved);
    }

    @Override
    public EventResponse cancelEvent(Long id, Long userId) {

        Event event = getOwnedActiveEvent(id, userId);

        if (event.getStatus() == EventStatus.CANCELLED) {
            return eventMapper.toResponse(event);
        }

        event.setStatus(EventStatus.CANCELLED);
        event.setDeletedAt(Instant.now());

        Event saved = eventRepository.save(event);

        log.info("Event cancelled id={}, userId={}", id, userId);

        return eventMapper.toResponse(saved);
    }


    @Override
    public void deleteEvent(Long id, Long userId) {

        Event event = getOwnedActiveEvent(id, userId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventDeletionNotAllowedException("Only DRAFT events can be deleted");
        }

        if (event.getEndTime().isBefore(LocalDateTime.now())) {
            throw new EventDeletionNotAllowedException("Cannot delete finished event");
        }

        event.setStatus(EventStatus.CANCELLED);
        event.setDeletedAt(Instant.now());

        eventRepository.save(event);

        log.info("Event deleted id={}, userId={}", id, userId);
    }

    private Event findActiveEvent(Long id) {

        return eventRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    private Event getOwnedActiveEvent(Long eventId, Long userId) {

        Event event = findActiveEvent(eventId);

        if (!event.getOrganizerId().equals(userId)) {
            throw new EventAccessDeniedException(eventId, userId);
        }

        return event;
    }

    private void ensureUpdatable(Event event) {

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new EventUpdateNotAllowedException("Cancelled event cannot be modified");
        }

        if (event.getStatus() == EventStatus.PUBLISHED) {
            throw new EventUpdateNotAllowedException("Published event cannot be fully modified");
        }
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