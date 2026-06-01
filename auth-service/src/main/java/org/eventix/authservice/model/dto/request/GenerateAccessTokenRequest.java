package org.eventix.authservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.eventix.authservice.model.enums.UserRole;

import java.util.Set;

public record GenerateAccessTokenRequest(

        @NotBlank
        String userId,

        @NotEmpty
        Set<UserRole> roles

) {
}