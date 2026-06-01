package org.eventix.authservice.mapper;

import org.eventix.authservice.model.dto.request.UserCreateRequest;
import org.eventix.authservice.model.dto.request.UserUpdateRequest;
import org.eventix.authservice.model.dto.response.UserResponse;
import org.eventix.authservice.model.entity.User;
import org.mapstruct.*;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserCreateRequest request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateUser(UserUpdateRequest request, @MappingTarget User user);


    @Mapping(source = "role", target = "role")
    @Mapping(source = "status", target = "status")
    UserResponse toResponse(User user);
}