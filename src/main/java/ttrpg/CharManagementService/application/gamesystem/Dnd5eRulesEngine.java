package ttrpg.CharManagementService.application.gamesystem;

import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemCodes;

@Service
public class Dnd5eRulesEngine implements GameSystemRulesEngine {

    private static final Set<String> ROOT_FIELDS = Set.of(
        "name",
        "playerName",
        "className",
        "subclass",
        "level",
        "background",
        "race",
        "alignment",
        "experiencePoints",
        "proficiencyBonus",
        "armorClass",
        "initiativeBonus",
        "speedFeet",
        "hitPoints",
        "hitDice",
        "abilities",
        "savingThrows",
        "skills",
        "senses",
        "languages",
        "proficiencies",
        "equipment",
        "featuresAndTraits",
        "spells",
        "notes"
    );

    private static final Set<String> ABILITY_FIELDS = Set.of(
        "strength",
        "dexterity",
        "constitution",
        "intelligence",
        "wisdom",
        "charisma"
    );

    private static final Set<String> HIT_POINT_FIELDS = Set.of("maximum", "current", "temporary");

    @Override
    public String getSystemCode() {
        return GameSystemCodes.DND5E;
    }

    @Override
    public JsonNode validateAndNormalize(JsonNode characterData, JsonNode templateSchema) {
        if (characterData == null || characterData.isNull() || !characterData.isObject()) {
            throw InvalidInputException.invalidValue("characterData", "characterData must be a JSON object");
        }

        var root = (ObjectNode) characterData;
        rejectUnexpectedFields(root, ROOT_FIELDS, "characterData");

        var normalized = JsonNodeFactory.instance.objectNode();
        normalized.put("name", readRequiredText(root, "name"));
        normalized.set("playerName", normalizeOptionalText(root.get("playerName"), "playerName"));
        normalized.put("className", readRequiredText(root, "className"));
        normalized.set("subclass", normalizeOptionalText(root.get("subclass"), "subclass"));
        normalized.put("level", normalizePositiveInt(root.get("level"), "level"));
        normalized.set("background", normalizeOptionalText(root.get("background"), "background"));
        normalized.set("race", normalizeOptionalText(root.get("race"), "race"));
        normalized.set("alignment", normalizeOptionalText(root.get("alignment"), "alignment"));
        normalized.set("experiencePoints", normalizeOptionalInt(root.get("experiencePoints"), "experiencePoints"));
        normalized.put("proficiencyBonus", normalizeRequiredInt(root.get("proficiencyBonus"), "proficiencyBonus"));
        normalized.set("armorClass", normalizeOptionalInt(root.get("armorClass"), "armorClass"));
        normalized.set("initiativeBonus", normalizeOptionalInt(root.get("initiativeBonus"), "initiativeBonus"));
        normalized.set("speedFeet", normalizeOptionalInt(root.get("speedFeet"), "speedFeet"));
        normalized.set("hitPoints", normalizeHitPoints(root.get("hitPoints")));
        normalized.set("hitDice", normalizeOptionalText(root.get("hitDice"), "hitDice"));
        normalized.set("abilities", normalizeAbilities(root.get("abilities")));
        normalized.set("savingThrows", normalizeOptionalIntMap(root.get("savingThrows"), "savingThrows"));
        normalized.set("skills", normalizeOptionalIntMap(root.get("skills"), "skills"));
        normalized.set("senses", normalizeOptionalStringArray(root.get("senses"), "senses"));
        normalized.set("languages", normalizeOptionalStringArray(root.get("languages"), "languages"));
        normalized.set("proficiencies", normalizeOptionalStringArray(root.get("proficiencies"), "proficiencies"));
        normalized.set("equipment", normalizeOptionalStringArray(root.get("equipment"), "equipment"));
        normalized.set("featuresAndTraits", normalizeOptionalStringArray(root.get("featuresAndTraits"), "featuresAndTraits"));
        normalized.set("spells", normalizeOptionalStringArray(root.get("spells"), "spells"));
        normalized.set("notes", normalizeOptionalText(root.get("notes"), "notes"));
        return normalized;
    }

    private ObjectNode normalizeHitPoints(JsonNode hitPointsNode) {
        var normalized = JsonNodeFactory.instance.objectNode();
        if (hitPointsNode == null || hitPointsNode.isNull()) {
            normalized.set("maximum", JsonNodeFactory.instance.nullNode());
            normalized.set("current", JsonNodeFactory.instance.nullNode());
            normalized.set("temporary", JsonNodeFactory.instance.nullNode());
            return normalized;
        }
        if (!hitPointsNode.isObject()) {
            throw InvalidInputException.invalidValue("hitPoints", "hitPoints must be an object");
        }

        var hitPoints = (ObjectNode) hitPointsNode;
        rejectUnexpectedFields(hitPoints, HIT_POINT_FIELDS, "hitPoints");
        normalized.set("maximum", normalizeOptionalInt(hitPoints.get("maximum"), "hitPoints.maximum"));
        normalized.set("current", normalizeOptionalInt(hitPoints.get("current"), "hitPoints.current"));
        normalized.set("temporary", normalizeOptionalInt(hitPoints.get("temporary"), "hitPoints.temporary"));
        return normalized;
    }

    private ObjectNode normalizeAbilities(JsonNode abilitiesNode) {
        if (abilitiesNode == null || !abilitiesNode.isObject()) {
            throw InvalidInputException.invalidValue("abilities", "abilities must be an object");
        }

        var abilities = (ObjectNode) abilitiesNode;
        rejectUnexpectedFields(abilities, ABILITY_FIELDS, "abilities");

        var normalized = JsonNodeFactory.instance.objectNode();
        normalized.put("strength", normalizeRequiredInt(abilities.get("strength"), "abilities.strength"));
        normalized.put("dexterity", normalizeRequiredInt(abilities.get("dexterity"), "abilities.dexterity"));
        normalized.put("constitution", normalizeRequiredInt(abilities.get("constitution"), "abilities.constitution"));
        normalized.put("intelligence", normalizeRequiredInt(abilities.get("intelligence"), "abilities.intelligence"));
        normalized.put("wisdom", normalizeRequiredInt(abilities.get("wisdom"), "abilities.wisdom"));
        normalized.put("charisma", normalizeRequiredInt(abilities.get("charisma"), "abilities.charisma"));
        return normalized;
    }

    private ObjectNode normalizeOptionalIntMap(JsonNode node, String fieldName) {
        var normalized = JsonNodeFactory.instance.objectNode();
        if (node == null || node.isNull()) {
            return normalized;
        }
        if (!node.isObject()) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must be an object");
        }

        var values = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        for (var entry : ((ObjectNode) node).properties()) {
            var key = normalizeKey(entry.getKey(), fieldName);
            var valueNode = entry.getValue();
            if (valueNode == null || !valueNode.canConvertToInt()) {
                throw InvalidInputException.invalidValue(fieldName + "." + key, fieldName + " values must be integers");
            }
            var previous = values.put(key, valueNode.intValue());
            if (previous != null) {
                throw InvalidInputException.invalidValue(fieldName + "." + key, "duplicate keys are not allowed");
            }
        }

        for (var entry : values.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue());
        }
        return normalized;
    }

    private ArrayNode normalizeOptionalStringArray(JsonNode node, String fieldName) {
        var normalized = JsonNodeFactory.instance.arrayNode();
        if (node == null || node.isNull()) {
            return normalized;
        }
        if (!node.isArray()) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must be an array");
        }

        for (int index = 0; index < node.size(); index++) {
            var item = node.get(index);
            if (item == null || !item.isTextual() || item.asText().isBlank()) {
                throw InvalidInputException.invalidValue(
                    fieldName + "[" + index + "]",
                    fieldName + " entries must be non-blank strings"
                );
            }
            normalized.add(item.asText().trim());
        }
        return normalized;
    }

    private JsonNode normalizeOptionalText(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return JsonNodeFactory.instance.nullNode();
        }
        if (!node.isTextual()) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must be a string or null");
        }
        var value = node.asText().trim();
        return value.isEmpty() ? JsonNodeFactory.instance.nullNode() : JsonNodeFactory.instance.textNode(value);
    }

    private JsonNode normalizeOptionalInt(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return JsonNodeFactory.instance.nullNode();
        }
        return JsonNodeFactory.instance.numberNode(normalizeRequiredInt(node, fieldName));
    }

    private int normalizePositiveInt(JsonNode node, String fieldName) {
        var value = normalizeRequiredInt(node, fieldName);
        if (value <= 0) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must be greater than 0");
        }
        return value;
    }

    private int normalizeRequiredInt(JsonNode node, String fieldName) {
        if (node == null || !node.canConvertToInt()) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must be an integer");
        }
        return node.intValue();
    }

    private String readRequiredText(ObjectNode objectNode, String fieldName) {
        var node = objectNode.get(fieldName);
        if (node == null || !node.isTextual() || node.asText().isBlank()) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must be a non-blank string");
        }
        return node.asText().trim();
    }

    private String normalizeKey(String rawKey, String fieldName) {
        var key = rawKey == null ? "" : rawKey.trim();
        if (key.isBlank()) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " keys must be non-blank");
        }
        return key;
    }

    private void rejectUnexpectedFields(ObjectNode objectNode, Set<String> allowedFields, String fieldName) {
        for (var entry : objectNode.properties()) {
            var actualFieldName = entry.getKey();
            if (!allowedFields.contains(actualFieldName)) {
                throw InvalidInputException.invalidValue(fieldName, "Unexpected field: " + actualFieldName);
            }
        }
    }
}
