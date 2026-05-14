package ttrpg.CharManagementService.integration.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
