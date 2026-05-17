package ttrpg.CharManagementService.presentation.dto;

import java.util.List;
import java.util.UUID;

public record RollDiceResponse(
    UUID characterId,
    String formula,
    String resolvedFormula,
    int total,
    List<RollTermResponse> terms
) {}
