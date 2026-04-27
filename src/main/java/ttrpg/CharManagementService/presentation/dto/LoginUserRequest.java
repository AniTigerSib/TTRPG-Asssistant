package ttrpg.CharManagementService.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginUserRequest(
    @NotBlank
    @Size(max = 255)
    String login,

    @NotBlank
    @Size(max = 255)
    String password
) {}
