package ttrpg.CharManagementService.application.dice;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.shared.CharacterAccessPolicy;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.character.CharacterRepository;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataRepository;
import ttrpg.CharManagementService.domain.exception.CharacterNotFoundException;
import ttrpg.CharManagementService.domain.exception.GameSystemNotFoundException;
import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemCodes;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemRepository;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;

@Service
@RequiredArgsConstructor
public class RollDiceUseCase {

    private static final Map<String, String> ABILITY_KEYS = Map.ofEntries(
        Map.entry("STR", "strength"),
        Map.entry("STRENGTH", "strength"),
        Map.entry("DEX", "dexterity"),
        Map.entry("DEXTERITY", "dexterity"),
        Map.entry("CON", "constitution"),
        Map.entry("CONSTITUTION", "constitution"),
        Map.entry("INT", "intelligence"),
        Map.entry("INTELLIGENCE", "intelligence"),
        Map.entry("WIS", "wisdom"),
        Map.entry("WISDOM", "wisdom"),
        Map.entry("CHA", "charisma"),
        Map.entry("CHARISMA", "charisma")
    );

    private final CharacterRepository characterRepository;
    private final CharacterDataRepository characterDataRepository;
    private final GameSystemRepository gameSystemRepository;
    private final CharacterAccessPolicy characterAccessPolicy;
    private final DiceFormulaEvaluator diceFormulaEvaluator;

    @Transactional(readOnly = true)
    public DiceRollResult execute(User currentUser, RollDiceCommand command) {
        Checkers.requireNonNull(currentUser, "currentUser");
        Checkers.requireNonNull(command, "command");
        return diceFormulaEvaluator.evaluate(command.formula(), null, null);
    }

    @Transactional(readOnly = true)
    public DiceRollResult execute(User currentUser, CharacterId characterId, RollDiceCommand command) {
        Checkers.requireNonNull(currentUser, "currentUser");
        Checkers.requireNonNull(characterId, "characterId");
        Checkers.requireNonNull(command, "command");

        var character = characterRepository.findById(characterId)
            .orElseThrow(() -> new CharacterNotFoundException(characterId.value()));
        characterAccessPolicy.assertCanView(currentUser, character);

        var characterData = characterDataRepository.findByCharacterId(characterId)
            .orElseThrow(() -> new CharacterNotFoundException(characterId.value()));
        var gameSystem = gameSystemRepository.findById(character.getGameSystemId())
            .orElseThrow(() -> new GameSystemNotFoundException(character.getGameSystemId().asString()));

        return diceFormulaEvaluator.evaluate(
            command.formula(),
            characterId.value(),
            placeholder -> resolveModifier(placeholder, gameSystem.getCode(), characterData.getData())
        );
    }

    private int resolveModifier(String placeholder, String gameSystemCode, JsonNode characterData) {
        if (!GameSystemCodes.DND5E.equalsIgnoreCase(gameSystemCode)) {
            throw InvalidInputException.invalidValue(
                "formula",
                "Character modifiers are only supported for DND5E characters"
            );
        }

        var normalized = placeholder.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("PROF") || normalized.equals("PB") || normalized.equals("PROFICIENCY_BONUS")) {
            var node = characterData.get("proficiencyBonus");
            if (node == null || !node.canConvertToInt()) {
                throw InvalidInputException.invalidValue(
                    "formula",
                    "Character does not define proficiencyBonus required by ["
                        + normalized
                        + "]"
                );
            }
            return node.intValue();
        }

        var abilityKey = ABILITY_KEYS.get(normalized);
        if (abilityKey == null) {
            throw InvalidInputException.invalidValue("formula", "Unsupported character modifier [" + normalized + "]");
        }

        var abilities = characterData.get("abilities");
        if (abilities == null || !abilities.isObject()) {
            throw InvalidInputException.invalidValue("formula", "Character does not define abilities required by [" + normalized + "]");
        }

        var abilityNode = abilities.get(abilityKey);
        if (abilityNode == null) {
            throw InvalidInputException.invalidValue("formula", "Character does not define [" + normalized + "]");
        }

        int score;
        if (abilityNode.canConvertToInt()) {
            score = abilityNode.intValue();
        } else if (abilityNode.isObject() && abilityNode.path("score").canConvertToInt()) {
            score = abilityNode.path("score").intValue();
        } else {
            throw InvalidInputException.invalidValue("formula", "Character does not define [" + normalized + "] as an integer score");
        }

        return Math.floorDiv(score - 10, 2);
    }
}
