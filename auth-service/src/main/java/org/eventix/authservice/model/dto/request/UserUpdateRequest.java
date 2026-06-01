package org.eventix.authservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Email(message = "Invalid email format")
        @Size(max = 255)
        String email,

        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password

) {}
