package ttrpg.CharManagementService.presentation.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpsertCharacterTemplateRequest(
    @NotNull UUID gameSystemId,
    @NotBlank String name,
    @NotNull Object schema,
    @Min(1) int version,
    boolean official,
    @NotBlank String visibility
) {}
