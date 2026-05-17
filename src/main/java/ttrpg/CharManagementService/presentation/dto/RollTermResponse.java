package ttrpg.CharManagementService.presentation.dto;

import java.util.List;

public record RollTermResponse(
    String kind,
    String notation,
    String resolvedNotation,
    int sign,
    List<Integer> rolls,
    int value
) {}
