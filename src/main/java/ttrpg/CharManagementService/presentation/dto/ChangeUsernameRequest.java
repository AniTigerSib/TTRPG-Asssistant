package ttrpg.CharManagementService.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeUsernameRequest(
    @NotBlank
    @Size(max = 100)
    String newUsername
) {}
