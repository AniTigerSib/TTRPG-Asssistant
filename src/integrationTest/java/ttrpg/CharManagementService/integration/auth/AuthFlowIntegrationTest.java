package ttrpg.CharManagementService.integration.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.integration.support.IntegrationTestSupport;

class AuthFlowIntegrationTest extends IntegrationTestSupport {

    @Test
    void healthEndpointReportsUp() throws Exception {
        var response = get("/actuator/health");

        assertEquals(200, response.statusCode(), response.describe());
        assertEquals("UP", response.body().path("status").asText(), response.describe());
    }

    @Test
    void registerAndLoginFlowWorks() throws Exception {
        var user = newTestUser();

        var registerResponse = register(user);

        assertEquals(201, registerResponse.statusCode(), registerResponse.describe());
        assertEquals(user.email(), registerResponse.body().path("email").asText(), registerResponse.describe());
        assertEquals(user.username(), registerResponse.body().path("username").asText(), registerResponse.describe());
        assertFalse(registerResponse.body().path("id").asText().isBlank(), registerResponse.describe());

        var loginResponse = login(user.username(), user.password());

        assertEquals(200, loginResponse.statusCode(), loginResponse.describe());
        assertFalse(loginResponse.body().path("accessToken").path("token").asText().isBlank(), loginResponse.describe());
        assertFalse(loginResponse.body().path("refreshToken").path("token").asText().isBlank(), loginResponse.describe());
    }

    @Test
    void refreshFlowWorks() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);

        var refreshResponse = refresh(session.refreshToken());

        assertEquals(200, refreshResponse.statusCode(), refreshResponse.describe());
        assertFalse(refreshResponse.body().path("accessToken").path("token").asText().isBlank(), refreshResponse.describe());
        assertFalse(refreshResponse.body().path("refreshToken").path("token").asText().isBlank(), refreshResponse.describe());
    }

    @Test
    void logoutInvalidatesTokens() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);

        var logoutResponse = logout(session.accessToken(), session.refreshToken());

        assertEquals(204, logoutResponse.statusCode(), logoutResponse.describe());

        var refreshAfterLogoutResponse = refresh(session.refreshToken());
        assertEquals(401, refreshAfterLogoutResponse.statusCode(), refreshAfterLogoutResponse.describe());
        assertEquals("INVALID_CREDENTIALS", refreshAfterLogoutResponse.body().path("code").asText(),
            refreshAfterLogoutResponse.describe());

        var protectedEndpointAfterLogoutResponse = changePassword(
            session.accessToken(),
            user.password(),
            "AnotherPass3"
        );
        assertEquals(401, protectedEndpointAfterLogoutResponse.statusCode(), protectedEndpointAfterLogoutResponse.describe());
        assertEquals("INVALID_CREDENTIALS", protectedEndpointAfterLogoutResponse.body().path("code").asText(),
            protectedEndpointAfterLogoutResponse.describe());
    }
}
