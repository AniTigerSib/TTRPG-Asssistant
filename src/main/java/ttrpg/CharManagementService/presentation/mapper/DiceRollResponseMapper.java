package ttrpg.CharManagementService.presentation.mapper;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.application.dice.DiceRollResult;
import ttrpg.CharManagementService.presentation.dto.RollDiceResponse;
import ttrpg.CharManagementService.presentation.dto.RollTermResponse;

@Component
public class DiceRollResponseMapper {

    public RollDiceResponse toResponse(DiceRollResult result) {
        return new RollDiceResponse(
            result.characterId(),
            result.formula(),
            result.resolvedFormula().trim(),
            result.total(),
            result.terms().stream()
                .map(term -> new RollTermResponse(
                    term.kind().name(),
                    term.notation(),
                    term.resolvedNotation(),
                    term.sign(),
                    term.rolls(),
                    term.value()
                ))
                .toList()
        );
    }
}
