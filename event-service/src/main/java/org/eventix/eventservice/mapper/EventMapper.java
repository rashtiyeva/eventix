package org.eventix.eventservice.mapper;

import org.eventix.eventservice.model.dto.request.EventCreateRequest;
import org.eventix.eventservice.model.dto.request.EventPatchRequest;
import org.eventix.eventservice.model.dto.request.EventUpdateRequest;
import org.eventix.eventservice.model.dto.response.EventPreviewResponse;
import org.eventix.eventservice.model.dto.response.EventResponse;
import org.eventix.eventservice.model.entity.Event;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EventMapper {

    EventResponse toResponse(Event event);

    EventPreviewResponse toPreview(Event event);

    Event toEntity(EventCreateRequest request);

    void updateFromRequest(EventUpdateRequest request,
                           @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchFromRequest(EventPatchRequest request,
                          @MappingTarget Event event);
}