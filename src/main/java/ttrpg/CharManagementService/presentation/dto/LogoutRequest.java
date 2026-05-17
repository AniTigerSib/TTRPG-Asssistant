package ttrpg.CharManagementService.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogoutRequest(
    @NotBlank
    @Size(max = 1024)
    String refreshToken
) {}
