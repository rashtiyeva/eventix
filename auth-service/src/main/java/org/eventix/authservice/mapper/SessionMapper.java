package org.eventix.authservice.mapper;

import org.eventix.authservice.model.dto.response.SessionResponse;
import org.eventix.authservice.model.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SessionMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "status", target = "status")
    SessionResponse toResponse(Session session);
}