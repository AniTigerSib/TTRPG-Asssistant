package ttrpg.CharManagementService.integration.character;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.integration.support.IntegrationTestSupport;

class CharacterFlowIntegrationTest extends IntegrationTestSupport {

    @Test
    void canCreateCharacterOutsideCampaignWithoutTemplate() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);

        var createResponse = post(
            "/api/v1/characters",
            orderedMap(
                "campaignId", null,
                "gameSystemCode", "FATE_CORE",
                "templateId", null,
                "name", "Lone Hero",
                "avatarUrl", null,
                "status", "draft",
                "data", orderedMap(
                    "freeform", true,
                    "notes", "no validation should run here"
                )
            ),
            bearer(session.accessToken())
        );

        assertEquals(200, createResponse.statusCode(), createResponse.describe());
        assertEquals("Lone Hero", createResponse.body().path("name").asText(), createResponse.describe());
        assertEquals("draft", createResponse.body().path("status").asText(), createResponse.describe());
        assertEquals(true, createResponse.body().path("campaignId").isNull(), createResponse.describe());
        assertEquals(true, createResponse.body().path("data").path("freeform").asBoolean(), createResponse.describe());
    }

    @Test
    void deniesReadingAndEditingCharacterToUnrelatedUser() throws Exception {
        var owner = newTestUser();
        var stranger = newTestUser();
        var ownerSession = registerAndLogin(owner);
        var strangerSession = registerAndLogin(stranger);

        var createResponse = post(
            "/api/v1/characters",
            orderedMap(
                "campaignId", null,
                "gameSystemCode", "FATE_CORE",
                "templateId", null,
                "name", "Private Hero",
                "avatarUrl", null,
                "status", "draft",
                "data", orderedMap("secret", "owner-only")
            ),
            bearer(ownerSession.accessToken())
        );
        assertEquals(200, createResponse.statusCode(), createResponse.describe());
        var characterId = createResponse.body().path("id").asText();

        var getResponse = get("/api/v1/characters/" + characterId, bearer(strangerSession.accessToken()));
        assertEquals(403, getResponse.statusCode(), getResponse.describe());
        assertEquals("ACCESS_DENIED", getResponse.body().path("code").asText(), getResponse.describe());

        var updateResponse = put(
            "/api/v1/characters/" + characterId,
            orderedMap(
                "name", "Hijacked Hero",
                "avatarUrl", null,
                "status", "active",
                "data", orderedMap("secret", "changed")
            ),
            bearer(strangerSession.accessToken())
        );
        assertEquals(403, updateResponse.statusCode(), updateResponse.describe());
        assertEquals("ACCESS_DENIED", updateResponse.body().path("code").asText(), updateResponse.describe());
    }

    @Test
    void campaignMemberCanViewCharacterFromSameCampaign() throws Exception {
        var owner = newTestUser();
        var player = newTestUser();
        var ownerSession = registerAndLogin(owner);
        var playerSession = registerAndLogin(player);

        var ownerId = ownerSession.loginResponse().body().path("user").path("id").asText();
        var playerId = playerSession.loginResponse().body().path("user").path("id").asText();

        var gameSystemId = findGameSystemIdByCode("FATE_CORE");
        var campaignId = createCampaign(java.util.UUID.fromString(ownerId), gameSystemId, "Shared Campaign", "private");
        addCampaignMember(campaignId, java.util.UUID.fromString(playerId), "PLAYER");

        var createResponse = post(
            "/api/v1/characters",
            orderedMap(
                "campaignId", campaignId,
                "gameSystemCode", "FATE_CORE",
                "templateId", null,
                "name", "Shared Hero",
                "avatarUrl", null,
                "status", "active",
                "data", orderedMap("notes", "campaign-visible")
            ),
            bearer(ownerSession.accessToken())
        );
        assertEquals(200, createResponse.statusCode(), createResponse.describe());

        var characterId = createResponse.body().path("id").asText();
        var getResponse = get("/api/v1/characters/" + characterId, bearer(playerSession.accessToken()));

        assertEquals(200, getResponse.statusCode(), getResponse.describe());
        assertEquals("Shared Hero", getResponse.body().path("name").asText(), getResponse.describe());
        assertEquals("campaign-visible", getResponse.body().path("data").path("notes").asText(), getResponse.describe());
    }

    @Test
    void canCreateAndUpdateDnd5eCharacterWithOfficialTemplate() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);
        var templateId = findTemplateIdByName("Official D&D 5e Character Sheet");

        var createResponse = post(
            "/api/v1/characters",
            orderedMap(
                "campaignId", null,
                "gameSystemCode", "DND5E",
                "templateId", templateId,
                "name", "Sister Arin",
                "avatarUrl", null,
                "status", "active",
                "data", orderedMap(
                    "name", "Sister Arin",
                    "playerName", "Mira",
                    "className", "Cleric",
                    "subclass", "Light Domain",
                    "level", 5,
                    "background", "Acolyte",
                    "race", "Human",
                    "alignment", "Neutral Good",
                    "experiencePoints", 6500,
                    "proficiencyBonus", 3,
                    "armorClass", 18,
                    "initiativeBonus", 1,
                    "speedFeet", 30,
                    "hitPoints", orderedMap(
                        "maximum", 38,
                        "current", 31,
                        "temporary", 0
                    ),
                    "hitDice", "5d8",
                    "abilities", orderedMap(
                        "strength", 10,
                        "dexterity", 12,
                        "constitution", 14,
                        "intelligence", 11,
                        "wisdom", 15,
                        "charisma", 13
                    ),
                    "savingThrows", orderedMap(
                        "wisdom", 6,
                        "charisma", 4
                    ),
                    "skills", orderedMap(
                        "Insight", 6,
                        "Perception", 6
                    ),
                    "senses", java.util.List.of("passive Perception 16"),
                    "languages", java.util.List.of("Common", "Celestial"),
                    "proficiencies", java.util.List.of("Light armor", "Simple weapons"),
                    "equipment", java.util.List.of("Shield", "Mace"),
                    "featuresAndTraits", java.util.List.of("Warding Flare"),
                    "spells", java.util.List.of("Guiding Bolt"),
                    "notes", "Front-line support"
                )
            ),
            bearer(session.accessToken())
        );

        assertEquals(200, createResponse.statusCode(), createResponse.describe());
        assertEquals("Cleric", createResponse.body().path("data").path("className").asText(), createResponse.describe());
        assertEquals(15, createResponse.body().path("data").path("abilities").path("wisdom").asInt(), createResponse.describe());

        var characterId = createResponse.body().path("id").asText();
        var updateResponse = put(
            "/api/v1/characters/" + characterId,
            orderedMap(
                "name", "Sister Arin",
                "avatarUrl", null,
                "status", "active",
                "data", orderedMap(
                    "name", "Sister Arin",
                    "playerName", "Mira",
                    "className", "Cleric",
                    "subclass", "Light Domain",
                    "level", 5,
                    "background", "Acolyte",
                    "race", "Human",
                    "alignment", "Neutral Good",
                    "experiencePoints", 6500,
                    "proficiencyBonus", 4,
                    "armorClass", 18,
                    "initiativeBonus", 1,
                    "speedFeet", 30,
                    "hitPoints", orderedMap(
                        "maximum", 38,
                        "current", 27,
                        "temporary", 5
                    ),
                    "hitDice", "5d8",
                    "abilities", orderedMap(
                        "strength", 10,
                        "dexterity", 12,
                        "constitution", 14,
                        "intelligence", 11,
                        "wisdom", 16,
                        "charisma", 13
                    ),
                    "savingThrows", orderedMap(
                        "wisdom", 7,
                        "charisma", 5
                    ),
                    "skills", orderedMap(
                        "Insight", 7,
                        "Perception", 7
                    ),
                    "senses", java.util.List.of("passive Perception 17"),
                    "languages", java.util.List.of("Common", "Celestial"),
                    "proficiencies", java.util.List.of("Light armor", "Simple weapons"),
                    "equipment", java.util.List.of("Shield", "Mace"),
                    "featuresAndTraits", java.util.List.of("Warding Flare"),
                    "spells", java.util.List.of("Guiding Bolt", "Beacon of Hope"),
                    "notes", "Updated after level-up"
                )
            ),
            bearer(session.accessToken())
        );

        assertEquals(200, updateResponse.statusCode(), updateResponse.describe());
        assertEquals(4, updateResponse.body().path("data").path("proficiencyBonus").asInt(), updateResponse.describe());
        assertEquals(16, updateResponse.body().path("data").path("abilities").path("wisdom").asInt(), updateResponse.describe());
        assertEquals(5, updateResponse.body().path("data").path("hitPoints").path("temporary").asInt(), updateResponse.describe());
    }

    @Test
    void canRollAgainstBoundDnd5eCharacter() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);
        var templateId = findTemplateIdByName("Official D&D 5e Character Sheet");

        var createResponse = post(
            "/api/v1/characters",
            orderedMap(
                "campaignId", null,
                "gameSystemCode", "DND5E",
                "templateId", templateId,
                "name", "Field Cleric",
                "avatarUrl", null,
                "status", "active",
                "data", orderedMap(
                    "name", "Field Cleric",
                    "playerName", "Mira",
                    "className", "Cleric",
                    "subclass", "Light Domain",
                    "level", 5,
                    "background", "Acolyte",
                    "race", "Human",
                    "alignment", "Neutral Good",
                    "experiencePoints", 6500,
                    "proficiencyBonus", 3,
                    "armorClass", 18,
                    "initiativeBonus", 1,
                    "speedFeet", 30,
                    "hitPoints", orderedMap(
                        "maximum", 38,
                        "current", 31,
                        "temporary", 0
                    ),
                    "hitDice", "5d8",
                    "abilities", orderedMap(
                        "strength", 10,
                        "dexterity", 12,
                        "constitution", 14,
                        "intelligence", 11,
                        "wisdom", 15,
                        "charisma", 13
                    ),
                    "savingThrows", orderedMap(),
                    "skills", orderedMap(),
                    "senses", java.util.List.of(),
                    "languages", java.util.List.of("Common"),
                    "proficiencies", java.util.List.of(),
                    "equipment", java.util.List.of(),
                    "featuresAndTraits", java.util.List.of(),
                    "spells", java.util.List.of(),
                    "notes", null
                )
            ),
            bearer(session.accessToken())
        );
        assertEquals(200, createResponse.statusCode(), createResponse.describe());

        var characterId = createResponse.body().path("id").asText();
        var rollResponse = post(
            "/api/v1/characters/" + characterId + "/rolls",
            orderedMap("formula", "d20 + [PROF] + [WIS]"),
            bearer(session.accessToken())
        );

        assertEquals(200, rollResponse.statusCode(), rollResponse.describe());
        assertEquals(characterId, rollResponse.body().path("characterId").asText(), rollResponse.describe());
        assertEquals("d20 + 3 + 2", rollResponse.body().path("resolvedFormula").asText(), rollResponse.describe());
        assertEquals(3, rollResponse.body().path("terms").get(1).path("value").asInt(), rollResponse.describe());
        assertEquals(2, rollResponse.body().path("terms").get(2).path("value").asInt(), rollResponse.describe());

        var total = rollResponse.body().path("total").asInt();
        org.junit.jupiter.api.Assertions.assertTrue(total >= 6 && total <= 25, rollResponse.describe());
    }

    @Test
    void supportsUnboundFateDiceAndRejectsUnboundCharacterModifiers() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);

        var fateRollResponse = post(
            "/api/v1/rolls",
            orderedMap("formula", "4dF + 2"),
            bearer(session.accessToken())
        );

        assertEquals(200, fateRollResponse.statusCode(), fateRollResponse.describe());
        assertEquals("4dF + 2", fateRollResponse.body().path("resolvedFormula").asText(), fateRollResponse.describe());
        assertEquals(4, fateRollResponse.body().path("terms").get(0).path("rolls").size(), fateRollResponse.describe());
        var total = fateRollResponse.body().path("total").asInt();
        org.junit.jupiter.api.Assertions.assertTrue(total >= -2 && total <= 6, fateRollResponse.describe());

        var invalidRollResponse = post(
            "/api/v1/rolls",
            orderedMap("formula", "d20 + [WIS]"),
            bearer(session.accessToken())
        );

        assertEquals(400, invalidRollResponse.statusCode(), invalidRollResponse.describe());
        assertEquals("INVALID_INPUT", invalidRollResponse.body().path("code").asText(), invalidRollResponse.describe());
    }
}
