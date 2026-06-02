package org.eventix.authservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eventix.authservice.model.entity.Session;
import org.eventix.authservice.model.entity.User;

public record CreateRefreshTokenRequest(
        @NotNull User user,
        @NotBlank Session session
) {}