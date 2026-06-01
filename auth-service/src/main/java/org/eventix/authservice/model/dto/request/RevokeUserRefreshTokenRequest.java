package org.eventix.authservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import org.eventix.authservice.model.entity.User;

public record RevokeUserRefreshTokenRequest(
        @NotNull User user
) {}