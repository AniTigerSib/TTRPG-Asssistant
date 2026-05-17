package ttrpg.CharManagementService.presentation.dto;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCharacterRequest(
    UUID campaignId,
    String gameSystemCode,
    UUID templateId,
    @NotBlank String name,
    String avatarUrl,
    @NotBlank String status,
    @NotNull Map<String, Object> data
) {}
