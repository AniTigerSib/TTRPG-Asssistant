package ttrpg.CharManagementService.application.gamesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;

class Dnd5eRulesEngineTest {

    private final Dnd5eRulesEngine rulesEngine = new Dnd5eRulesEngine();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void validatesAndNormalizesDndCharacterSheet() throws Exception {
        var result = rulesEngine.validateAndNormalize(validCharacter(), objectMapper.createObjectNode());

        assertEquals("Sister Arin", result.get("name").asText());
        assertEquals("Cleric", result.get("className").asText());
        assertEquals(5, result.get("level").asInt());
        assertEquals(3, result.get("proficiencyBonus").asInt());
        assertEquals(16, result.get("abilities").get("wisdom").asInt());
        assertEquals(2, result.get("languages").size());
        assertTrue(result.get("notes").isNull());
    }

    @Test
    void rejectsUnexpectedRootField() throws Exception {
        var invalidCharacter = validCharacter().deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) invalidCharacter).put("homebrewField", true);

        var exception = assertThrows(
            InvalidInputException.class,
            () -> rulesEngine.validateAndNormalize(invalidCharacter, objectMapper.createObjectNode())
        );

        assertEquals("Unexpected field: homebrewField", exception.getPublicMessage());
    }

    @Test
    void rejectsMissingAbilityScore() throws Exception {
        var invalidCharacter = validCharacter().deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) invalidCharacter.get("abilities")).remove("wisdom");

        var exception = assertThrows(
            InvalidInputException.class,
            () -> rulesEngine.validateAndNormalize(invalidCharacter, objectMapper.createObjectNode())
        );

        assertEquals("abilities.wisdom must be an integer", exception.getPublicMessage());
    }

    private com.fasterxml.jackson.databind.JsonNode validCharacter() throws Exception {
        return objectMapper.readTree("""
            {
              "name": "Sister Arin",
              "playerName": "Mira",
              "className": "Cleric",
              "subclass": "Light Domain",
              "level": 5,
              "background": "Acolyte",
              "race": "Human",
              "alignment": "Neutral Good",
              "experiencePoints": 6500,
              "proficiencyBonus": 3,
              "armorClass": 18,
              "initiativeBonus": 1,
              "speedFeet": 30,
              "hitPoints": {
                "maximum": 38,
                "current": 31,
                "temporary": 0
              },
              "hitDice": "5d8",
              "abilities": {
                "strength": 10,
                "dexterity": 12,
                "constitution": 14,
                "intelligence": 11,
                "wisdom": 16,
                "charisma": 13
              },
              "savingThrows": {
                "wisdom": 6,
                "charisma": 4
              },
              "skills": {
                "Insight": 6,
                "Perception": 6,
                "Religion": 3
              },
              "senses": [
                "passive Perception 16"
              ],
              "languages": [
                "Common",
                "Celestial"
              ],
              "proficiencies": [
                "Light armor",
                "Simple weapons"
              ],
              "equipment": [
                "Shield",
                "Mace"
              ],
              "featuresAndTraits": [
                "Warding Flare"
              ],
              "spells": [
                "Guiding Bolt",
                "Lesser Restoration"
              ],
              "notes": "   "
            }
            """);
    }
}
