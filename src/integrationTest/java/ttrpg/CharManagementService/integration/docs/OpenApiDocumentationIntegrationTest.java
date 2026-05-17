package ttrpg.CharManagementService.integration.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.integration.support.IntegrationTestSupport;

class OpenApiDocumentationIntegrationTest extends IntegrationTestSupport {

    @Test
    void openApiSpecIsAvailableWithoutAuthentication() throws Exception {
        var response = get("/v3/api-docs");

        assertEquals(200, response.statusCode(), response.describe());
        assertFalse(response.body().path("openapi").asText().isBlank(), response.describe());
        assertFalse(response.body().path("info").path("title").asText().isBlank(), response.describe());
        assertFalse(response.body().path("paths").path("/api/v1/auth/login").isMissingNode(), response.describe());
    }
}
