package ttrpg.CharManagementService.application.dice;

import java.util.List;

public record DiceTermResult(
    DiceTermKind kind,
    String notation,
    String resolvedNotation,
    int sign,
    List<Integer> rolls,
    int value
) {}
