package org.eventix.authservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorRequest(
        @NotBlank String tempToken,
        @NotBlank String code
) {}
