package ttrpg.CharManagementService.presentation.dto;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

public record ValidateCharacterRequest(
    @NotNull Map<String, Object> characterData
) {}
