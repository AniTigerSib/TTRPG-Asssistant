package ttrpg.CharManagementService.integration.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

class AuthFlowIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = System.getProperty(
        "integration.base-url",
        System.getenv().getOrDefault("INTEGRATION_BASE_URL", "http://localhost:8080")
    );

    @Test
    void healthEndpointReportsUp() throws Exception {
        var response = get("/actuator/health");

        assertEquals(200, response.statusCode(), response.describe());
        assertEquals("UP", response.body().path("status").asText(), response.describe());
    }

    @Test
    void authenticationFlowWorksEndToEnd() throws Exception {
        var uniqueSuffix = UUID.randomUUID().toString().replace("-", "");
        var email = "it-" + uniqueSuffix + "@example.com";
        var username = "it" + uniqueSuffix.substring(0, 12);
        var initialPassword = "StrongPass1";
        var changedPassword = "ChangedPass2";

        var registerResponse = post(
            "/api/v1/auth/register",
            orderedMap(
                "email", email,
                "username", username,
                "password", initialPassword
            ),
            Map.of()
        );

        assertEquals(201, registerResponse.statusCode(), registerResponse.describe());
        assertEquals(email, registerResponse.body().path("email").asText(), registerResponse.describe());
        assertEquals(username, registerResponse.body().path("username").asText(), registerResponse.describe());
        assertFalse(registerResponse.body().path("id").asText().isBlank(), registerResponse.describe());

        var loginResponse = post(
            "/api/v1/auth/login",
            orderedMap(
                "login", username,
                "password", initialPassword
            ),
            Map.of()
        );

        assertEquals(200, loginResponse.statusCode(), loginResponse.describe());

        var accessToken = loginResponse.body().path("accessToken").path("token").asText();
        var refreshToken = loginResponse.body().path("refreshToken").path("token").asText();

        assertFalse(accessToken.isBlank(), loginResponse.describe());
        assertFalse(refreshToken.isBlank(), loginResponse.describe());

        var changePasswordResponse = post(
            "/api/v1/auth/change-password",
            orderedMap(
                "oldPassword", initialPassword,
                "newPassword", changedPassword
            ),
            Map.of("Authorization", "Bearer " + accessToken)
        );

        assertEquals(204, changePasswordResponse.statusCode(), changePasswordResponse.describe());

        var failedLoginResponse = post(
            "/api/v1/auth/login",
            orderedMap(
                "login", email,
                "password", initialPassword
            ),
            Map.of()
        );

        assertEquals(401, failedLoginResponse.statusCode(), failedLoginResponse.describe());
        assertEquals("INVALID_CREDENTIALS", failedLoginResponse.body().path("code").asText(), failedLoginResponse.describe());

        var reloginResponse = post(
            "/api/v1/auth/login",
            orderedMap(
                "login", email,
                "password", changedPassword
            ),
            Map.of()
        );

        assertEquals(200, reloginResponse.statusCode(), reloginResponse.describe());

        var refreshedAccessToken = reloginResponse.body().path("accessToken").path("token").asText();
        var refreshedRefreshToken = reloginResponse.body().path("refreshToken").path("token").asText();

        var logoutResponse = post(
            "/api/v1/auth/logout",
            orderedMap("refreshToken", refreshedRefreshToken),
            Map.of("Authorization", "Bearer " + refreshedAccessToken)
        );

        assertEquals(204, logoutResponse.statusCode(), logoutResponse.describe());

        var refreshAfterLogoutResponse = post(
            "/api/v1/auth/refresh",
            orderedMap("refreshToken", refreshedRefreshToken),
            Map.of()
        );

        assertEquals(401, refreshAfterLogoutResponse.statusCode(), refreshAfterLogoutResponse.describe());
        assertEquals("INVALID_CREDENTIALS", refreshAfterLogoutResponse.body().path("code").asText(),
            refreshAfterLogoutResponse.describe());

        var protectedEndpointAfterLogoutResponse = post(
            "/api/v1/auth/change-password",
            orderedMap(
                "oldPassword", changedPassword,
                "newPassword", "AnotherPass3"
            ),
            Map.of("Authorization", "Bearer " + refreshedAccessToken)
        );

        assertEquals(401, protectedEndpointAfterLogoutResponse.statusCode(), protectedEndpointAfterLogoutResponse.describe());
        assertEquals("INVALID_CREDENTIALS", protectedEndpointAfterLogoutResponse.body().path("code").asText(),
            protectedEndpointAfterLogoutResponse.describe());
    }

    private HttpJsonResponse get(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .header("Accept", "application/json")
            .GET()
            .build();

        return send(request);
    }

    private HttpJsonResponse post(String path, Object payload, Map<String, String> headers)
        throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json");

        headers.forEach(requestBuilder::header);

        var request = requestBuilder
            .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(payload)))
            .build();

        return send(request);
    }

    private HttpJsonResponse send(HttpRequest request) throws IOException, InterruptedException {
        var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return new HttpJsonResponse(response.statusCode(), parseBody(response.body()), response.body());
    }

    private JsonNode parseBody(String body) throws IOException {
        if (body == null || body.isBlank()) {
            return NullNode.getInstance();
        }
        return OBJECT_MAPPER.readTree(body);
    }

    private Map<String, Object> orderedMap(Object... pairs) {
        var values = new LinkedHashMap<String, Object>();
        for (var index = 0; index < pairs.length; index += 2) {
            values.put((String) pairs[index], pairs[index + 1]);
        }
        return values;
    }

    private record HttpJsonResponse(int statusCode, JsonNode body, String rawBody) {
        private String describe() {
            return "status=" + statusCode + ", body=" + rawBody;
        }
    }
}
