package ttrpg.CharManagementService.integration.gamesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.integration.support.IntegrationTestSupport;

class TemplateManagementIntegrationTest extends IntegrationTestSupport {

    @Test
    void normalUserCannotCreateTemplate() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);
        var gameSystemId = findGameSystemIdByCode("FATE_CORE");

        var response = post(
            "/api/v1/character-templates",
            orderedMap(
                "gameSystemId", gameSystemId,
                "name", "Forbidden Template",
                "schema", orderedMap("type", "fate-core.character-sheet"),
                "version", 1,
                "official", false,
                "visibility", "testing"
            ),
            bearer(session.accessToken())
        );

        assertEquals(403, response.statusCode(), response.describe());
        assertEquals("ACCESS_DENIED", response.body().path("code").asText(), response.describe());
    }

    @Test
    void creatorCanCreateAndUpdateTemplate() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);
        var userId = UUID.fromString(session.loginResponse().body().path("user").path("id").asText());
        var gameSystemId = findGameSystemIdByCode("FATE_CORE");
        grantUserRole(userId, "CREATOR");

        var createResponse = post(
            "/api/v1/character-templates",
            orderedMap(
                "gameSystemId", gameSystemId,
                "name", "Creator Template",
                "schema", orderedMap("type", "fate-core.character-sheet", "fields", java.util.List.of()),
                "version", 1,
                "official", false,
                "visibility", "testing"
            ),
            bearer(session.accessToken())
        );

        assertEquals(200, createResponse.statusCode(), createResponse.describe());
        assertEquals("testing", createResponse.body().path("visibility").asText(), createResponse.describe());

        var templateId = createResponse.body().path("id").asText();
        var updateResponse = put(
            "/api/v1/character-templates/" + templateId,
            orderedMap(
                "gameSystemId", gameSystemId,
                "name", "Creator Template Updated",
                "schema", orderedMap("type", "fate-core.character-sheet", "fields", java.util.List.of()),
                "version", 2,
                "official", true,
                "visibility", "draft"
            ),
            bearer(session.accessToken())
        );

        assertEquals(200, updateResponse.statusCode(), updateResponse.describe());
        assertEquals("Creator Template Updated", updateResponse.body().path("name").asText(), updateResponse.describe());
        assertEquals("draft", updateResponse.body().path("visibility").asText(), updateResponse.describe());
        assertEquals(2, updateResponse.body().path("version").asInt(), updateResponse.describe());
    }

    @Test
    void publicTemplateListingHidesTestingAndDraftTemplates() throws Exception {
        var gameSystemId = findGameSystemIdByCode("FATE_CORE");
        insertTemplate(
            UUID.randomUUID(),
            gameSystemId,
            "Hidden Testing Template",
            "{\"type\":\"fate-core.character-sheet\"}",
            1,
            false,
            "testing"
        );
        insertTemplate(
            UUID.randomUUID(),
            gameSystemId,
            "Hidden Draft Template",
            "{\"type\":\"fate-core.character-sheet\"}",
            1,
            false,
            "draft"
        );

        var publicResponse = get("/api/v1/game-systems/FATE_CORE/templates");

        assertEquals(200, publicResponse.statusCode(), publicResponse.describe());
        for (var item : publicResponse.body()) {
            assertEquals("visible", item.path("visibility").asText(), publicResponse.describe());
        }
    }
}
