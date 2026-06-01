package org.eventix.authservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValidateAccessTokenRequest(

        @NotBlank
        String token

) {
}