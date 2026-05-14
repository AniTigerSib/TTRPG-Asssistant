package ttrpg.CharManagementService.application.gamesystem;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemCodes;

@Service
public class FateCoreRulesEngine implements GameSystemRulesEngine {

    @Override
    public String getSystemCode() {
        return GameSystemCodes.FATE_CORE;
    }

    @Override
    public JsonNode validateAndNormalize(JsonNode characterData, JsonNode templateSchema) {
        if (characterData == null || characterData.isNull() || !characterData.isObject()) {
            throw InvalidInputException.invalidValue("characterData", "characterData must be a JSON object");
        }

        var schema = FateCoreSchema.from(templateSchema);
        var root = (ObjectNode) characterData;
        rejectUnexpectedFields(root, schema.allowedRootFields(), "characterData");

        var normalized = JsonNodeFactory.instance.objectNode();
        normalized.put("name", readRequiredText(root, "name"));
        normalized.set("description", normalizeOptionalText(root.get("description"), "description"));
        normalized.set("aspects", normalizeAspects(root.get("aspects"), schema));
        normalized.set("skills", normalizeSkills(root.get("skills")));
        normalized.set("stunts", normalizeStringArray(root.get("stunts"), "stunts"));
        normalized.put("refresh", normalizeRefresh(root.get("refresh"), schema));
        normalized.set("stress", normalizeStress(root.get("stress"), schema));
        normalized.set("consequences", normalizeConsequences(root.get("consequences"), schema));
        normalized.set("notes", normalizeOptionalText(root.get("notes"), "notes"));

        return normalized;
    }

    private ObjectNode normalizeAspects(JsonNode aspectsNode, FateCoreSchema schema) {
        if (aspectsNode == null || !aspectsNode.isObject()) {
            throw InvalidInputException.invalidValue("aspects", "aspects must be an object");
        }

        var aspects = (ObjectNode) aspectsNode;
        rejectUnexpectedFields(aspects, schema.allowedAspectFields(), "aspects");

        var normalizedAspects = JsonNodeFactory.instance.objectNode();
        normalizedAspects.put("highConcept", readRequiredText(aspects, "highConcept"));
        normalizedAspects.put("trouble", readRequiredText(aspects, "trouble"));
        normalizedAspects.set("additional", normalizeOptionalStringArray(aspects.get("additional"), "aspects.additional"));
        normalizedAspects.set("temporary", normalizeOptionalStringArray(aspects.get("temporary"), "aspects.temporary"));
        normalizedAspects.set("scene", normalizeOptionalStringArray(aspects.get("scene"), "aspects.scene"));
        return normalizedAspects;
    }

    private ObjectNode normalizeSkills(JsonNode skillsNode) {
        if (skillsNode == null || !skillsNode.isObject()) {
            throw InvalidInputException.invalidValue("skills", "skills must be an object");
        }

        var normalizedSkills = JsonNodeFactory.instance.objectNode();
        var skillValues = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);

        for (var entry : ((ObjectNode) skillsNode).properties()) {
            var skillName = normalizeSkillName(entry.getKey());
            if (!entry.getValue().canConvertToInt()) {
                throw InvalidInputException.invalidValue("skills." + skillName, "skill rank must be an integer");
            }

            var previous = skillValues.put(skillName, entry.getValue().intValue());
            if (previous != null) {
                throw InvalidInputException.invalidValue(
                    "skills." + skillName,
                    "duplicate skill names are not allowed"
                );
            }
        }

        for (var entry : skillValues.entrySet()) {
            normalizedSkills.put(entry.getKey(), entry.getValue());
        }
        return normalizedSkills;
    }

    private int normalizeRefresh(JsonNode refreshNode, FateCoreSchema schema) {
        var refresh = readRequiredInt(refreshNode, "refresh");
        if (refresh < schema.minRefresh() || refresh > schema.maxRefresh()) {
            throw InvalidInputException.invalidValue(
                "refresh",
                "refresh must be between " + schema.minRefresh() + " and " + schema.maxRefresh()
            );
        }
        return refresh;
    }

    private ObjectNode normalizeStress(JsonNode stressNode, FateCoreSchema schema) {
        if (stressNode == null || !stressNode.isObject()) {
            throw InvalidInputException.invalidValue("stress", "stress must be an object");
        }

        var stress = (ObjectNode) stressNode;
        rejectUnexpectedFields(stress, schema.allowedStressFields(), "stress");

        var normalizedStress = JsonNodeFactory.instance.objectNode();
        normalizedStress.put(
            "physical",
            normalizeBoundedInt(stress.get("physical"), "stress.physical", schema.minStress(), schema.maxStress())
        );
        normalizedStress.put(
            "mental",
            normalizeBoundedInt(stress.get("mental"), "stress.mental", schema.minStress(), schema.maxStress())
        );
        return normalizedStress;
    }

    private ObjectNode normalizeConsequences(JsonNode consequencesNode, FateCoreSchema schema) {
        if (consequencesNode == null || !consequencesNode.isObject()) {
            throw InvalidInputException.invalidValue("consequences", "consequences must be an object");
        }

        var consequences = (ObjectNode) consequencesNode;
        rejectUnexpectedFields(consequences, schema.allowedConsequenceFields(), "consequences");

        var normalizedConsequences = JsonNodeFactory.instance.objectNode();
        normalizedConsequences.set(
            "mild",
            normalizeLimitedNullableStringArray(consequences.get("mild"), "consequences.mild", schema.mildConsequenceSlots())
        );
        normalizedConsequences.set("moderate", normalizeOptionalText(consequences.get("moderate"), "consequences.moderate"));
        normalizedConsequences.set("severe", normalizeOptionalText(consequences.get("severe"), "consequences.severe"));
        return normalizedConsequences;
    }

    private ArrayNode normalizeStringArray(JsonNode node, String fieldName) {
        if (node == null || !node.isArray()) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must be an array");
        }
        return normalizeOptionalStringArray(node, fieldName);
    }

    private ArrayNode normalizeOptionalStringArray(JsonNode node, String fieldName) {
        var normalizedValues = JsonNodeFactory.instance.arrayNode();
        if (node == null || node.isNull()) {
            return normalizedValues;
        }
        if (!node.isArray()) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must be an array");
        }

        for (int index = 0; index < node.size(); index++) {
            var item = node.get(index);
            if (item == null || !item.isString() || item.asString().isBlank()) {
                throw InvalidInputException.invalidValue(
                    fieldName + "[" + index + "]",
                    fieldName + " entries must be non-blank strings"
                );
            }
            normalizedValues.add(item.asString().trim());
        }
        return normalizedValues;
    }

    private ArrayNode normalizeLimitedNullableStringArray(JsonNode node, String fieldName, int maxItems) {
        var normalizedValues = JsonNodeFactory.instance.arrayNode();
        if (node == null || node.isNull()) {
            return normalizedValues;
        }
        if (!node.isArray()) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must be an array");
        }
        if (node.size() > maxItems) {
            throw InvalidInputException.invalidValue(fieldName, fieldName + " must contain at most " + maxItems + " items");
        }

        for (int index = 0; index < node.size(); index++) {
            normalizedValues.add(normalizeOptionalText(node.get(index), fieldName + "[" + index + "]"));
        }
        return normalizedValues;
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

    private int normalizeBoundedInt(JsonNode node, String fieldName, int minValue, int maxValue) {
        var value = readRequiredInt(node, fieldName);
        if (value < minValue || value > maxValue) {
            throw InvalidInputException.invalidValue(
                fieldName,
                fieldName + " must be between " + minValue + " and " + maxValue
            );
        }
        return value;
    }

    private int readRequiredInt(JsonNode node, String fieldName) {
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

    private String normalizeSkillName(String rawSkillName) {
        var normalized = rawSkillName == null ? "" : rawSkillName.trim();
        if (normalized.isBlank()) {
            throw InvalidInputException.invalidValue("skills", "skill names must not be blank");
        }
        return normalized;
    }

    private void rejectUnexpectedFields(ObjectNode objectNode, Set<String> allowedFields, String fieldName) {
        for (var entry : objectNode.properties()) {
            var actualFieldName = entry.getKey();
            if (!allowedFields.contains(actualFieldName)) {
                throw InvalidInputException.invalidValue(fieldName, "Unexpected field: " + actualFieldName);
            }
        }
    }

    private record FateCoreSchema(
        int minRefresh,
        int maxRefresh,
        int minStress,
        int maxStress,
        int mildConsequenceSlots,
        Set<String> allowedRootFields,
        Set<String> allowedAspectFields,
        Set<String> allowedStressFields,
        Set<String> allowedConsequenceFields
    ) {
        private static final Set<String> DEFAULT_ROOT_FIELDS = Set.of(
            "name",
            "description",
            "aspects",
            "skills",
            "stunts",
            "refresh",
            "stress",
            "consequences",
            "notes"
        );
        private static final Set<String> DEFAULT_ASPECT_FIELDS = Set.of(
            "highConcept",
            "trouble",
            "additional",
            "temporary",
            "scene"
        );
        private static final Set<String> DEFAULT_STRESS_FIELDS = Set.of("physical", "mental");
        private static final Set<String> DEFAULT_CONSEQUENCE_FIELDS = Set.of("mild", "moderate", "severe");

        private static FateCoreSchema from(JsonNode templateSchema) {
            var rules = templateSchema == null ? JsonNodeFactory.instance.objectNode() : templateSchema.path("rules");
            return new FateCoreSchema(
                readIntOrDefault(rules.path("refresh"), "min", 0),
                readIntOrDefault(rules.path("refresh"), "max", 10),
                readIntOrDefault(rules.path("stress"), "min", 0),
                readIntOrDefault(rules.path("stress"), "max", 4),
                readIntOrDefault(rules.path("consequences"), "mildSlots", 2),
                readFieldNames(templateSchema, "fields", DEFAULT_ROOT_FIELDS),
                readFieldPropertyNames(templateSchema, "aspects", DEFAULT_ASPECT_FIELDS),
                readFieldPropertyNames(templateSchema, "stress", DEFAULT_STRESS_FIELDS),
                readFieldPropertyNames(templateSchema, "consequences", DEFAULT_CONSEQUENCE_FIELDS)
            );
        }

        private static int readIntOrDefault(JsonNode parent, String fieldName, int defaultValue) {
            var node = parent == null ? null : parent.get(fieldName);
            return node != null && node.canConvertToInt() ? node.intValue() : defaultValue;
        }

        private static Set<String> readFieldNames(JsonNode templateSchema, String fieldContainerName, Set<String> defaults) {
            var fieldsNode = templateSchema == null ? null : templateSchema.get(fieldContainerName);
            if (fieldsNode == null || !fieldsNode.isArray()) {
                return defaults;
            }

            var fieldNames = new LinkedHashSet<String>();
            for (var fieldNode : fieldsNode) {
                var nameNode = fieldNode.get("name");
                if (nameNode != null && nameNode.isTextual() && !nameNode.asText().isBlank()) {
                    fieldNames.add(nameNode.asText().trim());
                }
            }
            return fieldNames.isEmpty() ? defaults : Set.copyOf(fieldNames);
        }

        private static Set<String> readFieldPropertyNames(JsonNode templateSchema, String fieldName, Set<String> defaults) {
            var fieldDefinition = findField(templateSchema, fieldName);
            if (fieldDefinition == null) {
                return defaults;
            }

            var propertiesNode = fieldDefinition.get("properties");
            if (propertiesNode == null || !propertiesNode.isObject()) {
                return defaults;
            }

            var propertyNames = new LinkedHashSet<String>();
            for (var entry : ((ObjectNode) propertiesNode).properties()) {
                propertyNames.add(entry.getKey());
            }
            return propertyNames.isEmpty() ? defaults : Set.copyOf(propertyNames);
        }

        private static JsonNode findField(JsonNode templateSchema, String fieldName) {
            var fieldsNode = templateSchema == null ? null : templateSchema.get("fields");
            if (fieldsNode == null || !fieldsNode.isArray()) {
                return null;
            }

            for (var fieldNode : fieldsNode) {
                var nameNode = fieldNode.get("name");
                if (nameNode != null && nameNode.isTextual() && fieldName.equals(nameNode.asText())) {
                    return fieldNode;
                }
            }
            return null;
        }
    }
}
