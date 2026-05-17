package ttrpg.CharManagementService.application.dice;

import java.util.List;
import java.util.UUID;

public record DiceRollResult(
    UUID characterId,
    String formula,
    String resolvedFormula,
    int total,
    List<DiceTermResult> terms
) {}
