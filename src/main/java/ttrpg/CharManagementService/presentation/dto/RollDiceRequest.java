package ttrpg.CharManagementService.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record RollDiceRequest(@NotBlank String formula) {}
