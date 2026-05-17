package ttrpg.CharManagementService.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank
    @Size(max = 255)
    String oldPassword,

    @NotBlank
    @Size(max = 255)
    String newPassword
) {}
