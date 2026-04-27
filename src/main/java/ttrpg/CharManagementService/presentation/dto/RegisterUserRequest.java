package ttrpg.CharManagementService.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    @NotBlank
    @Size(max = 100)
    String username,

    @NotBlank
    @Size(max = 255)
    String password
) {}
