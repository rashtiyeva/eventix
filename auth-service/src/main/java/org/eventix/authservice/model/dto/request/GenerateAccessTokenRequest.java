package org.eventix.authservice.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eventix.authservice.model.enums.UserRole;
import java.util.Set;

public record GenerateAccessTokenRequest(

        @NotNull
        Long userId,

        @NotEmpty
        Set<UserRole> roles

) {
}