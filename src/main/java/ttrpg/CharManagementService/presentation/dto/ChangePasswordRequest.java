package ttrpg.CharManagementService.presentation.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotNull
    UUID userId,

    @NotBlank
    @Size(max = 255)
    String oldPassword,

    @NotBlank
    @Size(max = 255)
    String newPassword
) {}
