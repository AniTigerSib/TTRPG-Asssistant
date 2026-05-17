package ttrpg.CharManagementService.presentation.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCharacterRequest(
    @NotBlank String name,
    String avatarUrl,
    @NotBlank String status,
    @NotNull Map<String, Object> data
) {}
