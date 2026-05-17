package ttrpg.CharManagementService.presentation.dto;

import java.util.UUID;

public record ValidateCharacterResponse(
    UUID templateId,
    String gameSystemCode,
    String templateName,
    Object normalizedCharacterData
) {}
