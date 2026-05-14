package ttrpg.CharManagementService.application.gamesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import tools.jackson.databind.ObjectMapper;

class FateCoreRulesEngineTest {

    private final FateCoreRulesEngine rulesEngine = new FateCoreRulesEngine();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void validatesAndNormalizesStartingCharacter() throws Exception {
        var result = rulesEngine.validateAndNormalize(validCharacter(), templateSchema());

        assertEquals("Tara Vale", result.get("name").asText());
        assertEquals("Veteran Monster Hunter", result.get("aspects").get("highConcept").asText());
        assertEquals(6, result.get("skills").get("Physique").asInt());
        assertEquals(4, result.get("stress").get("physical").asInt());
        assertEquals(1, result.get("stress").get("mental").asInt());
        assertEquals(3, result.get("refresh").asInt());
        assertEquals(2, result.get("consequences").get("mild").size());
        assertTrue(result.get("consequences").get("moderate").isNull());
    }

    @Test
    void rejectsRefreshOutsideSchemaRange() throws Exception {
        var invalidCharacter = objectMapper.readTree("""
            {
              "name": "Tara Vale",
              "aspects": {
                "highConcept": "Veteran Monster Hunter",
                "trouble": "Trouble Follows My Curiosity"
              },
              "skills": {
                "Physique": 6,
                "Investigate": 5
              },
              "stunts": [
                "Because I Know Forgotten Rituals, once per session I can declare a useful occult detail."
              ],
              "refresh": 12,
              "stress": {
                "physical": 4,
                "mental": 1
              },
              "consequences": {
                "mild": [],
                "moderate": null,
                "severe": null
              }
            }
            """);

        var exception = assertThrows(
            InvalidInputException.class,
            () -> rulesEngine.validateAndNormalize(invalidCharacter, templateSchema())
        );

        assertEquals("refresh must be between 0 and 10", exception.getPublicMessage());
    }

    @Test
    void rejectsStressOutsideConfiguredBounds() throws Exception {
        var invalidCharacter = validCharacter().deepCopy();
        ((tools.jackson.databind.node.ObjectNode) invalidCharacter.get("stress")).put("mental", 5);

        var exception = assertThrows(
            InvalidInputException.class,
            () -> rulesEngine.validateAndNormalize(invalidCharacter, templateSchema())
        );

        assertEquals(
            "stress.mental must be between 0 and 4",
            exception.getPublicMessage()
        );
    }

    private tools.jackson.databind.JsonNode validCharacter() throws Exception {
        return objectMapper.readTree("""
            {
              "name": "Tara Vale",
              "description": "Occult detective for hire",
              "aspects": {
                "highConcept": "Veteran Monster Hunter",
                "trouble": "Trouble Follows My Curiosity",
                "additional": [
                  "Bound To The Last Lantern Society",
                  "I Trust The Wrong Witnesses",
                  "Every Relic Has A Price"
                ],
                "temporary": [
                  "Blessed By Candle Smoke"
                ],
                "scene": [
                  "Warehouse Full Of Shadows"
                ]
              },
              "skills": {
                "Physique": 6,
                "Will": 2,
                "Contacts": 5,
                "Notice": 4,
                "Athletics": 3,
                "Lore": 7,
                "Fight": 2,
                "Empathy": 1,
                "Rapport": 3,
                "Stealth": 2
              },
              "stunts": [
                "Because I Know Forgotten Rituals, once per session I can declare a useful occult detail.",
                "Because I Keep Hidden Silver, I gain +2 to Fight when ambushing monsters.",
                "Because I Read A Room Fast, I gain +2 to Empathy when sizing up a suspect."
              ],
              "refresh": 3,
              "stress": {
                "physical": 4,
                "mental": 1
              },
              "consequences": {
                "mild": ["Bruised Ribs", null],
                "moderate": null,
                "severe": null
              },
              "notes": "Keeps a ledger of debts."
            }
            """);
    }

    private tools.jackson.databind.JsonNode templateSchema() throws Exception {
        return objectMapper.readTree("""
            {
              "type": "fate-core.character-sheet",
              "rules": {
                "refresh": {
                  "min": 0,
                  "max": 10
                },
                "stress": {
                  "min": 0,
                  "max": 4
                },
                "consequences": {
                  "mildSlots": 2
                }
              },
              "fields": [
                {
                  "name": "name"
                },
                {
                  "name": "description"
                },
                {
                  "name": "aspects",
                  "properties": {
                    "highConcept": "string",
                    "trouble": "string",
                    "additional": "string[]",
                    "temporary": "string[]",
                    "scene": "string[]"
                  }
                },
                {
                  "name": "skills"
                },
                {
                  "name": "stunts"
                },
                {
                  "name": "refresh"
                },
                {
                  "name": "stress",
                  "properties": {
                    "physical": "int",
                    "mental": "int"
                  }
                },
                {
                  "name": "consequences",
                  "properties": {
                    "mild": "string[]|null",
                    "moderate": "string|null",
                    "severe": "string|null"
                  }
                },
                {
                  "name": "notes"
                }
              ]
            }
            """);
    }
}
