package ttrpg.CharManagementService.integration.support;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

public abstract class IntegrationTestSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = System.getProperty(
        "integration.base-url",
        System.getenv().getOrDefault("INTEGRATION_BASE_URL", "http://localhost:8080")
    );

    protected TestUser newTestUser() {
        var uniqueSuffix = UUID.randomUUID().toString().replace("-", "");
        return new TestUser(
            "it-" + uniqueSuffix + "@example.com",
            "it" + uniqueSuffix.substring(0, 12),
            "StrongPass1"
        );
    }

    protected HttpJsonResponse get(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .header("Accept", "application/json")
            .GET()
            .build();

        return send(request);
    }

    protected HttpJsonResponse register(TestUser user) throws IOException, InterruptedException {
        return post(
            "/api/v1/auth/register",
            orderedMap(
                "email", user.email(),
                "username", user.username(),
                "password", user.password()
            ),
            Map.of()
        );
    }

    protected HttpJsonResponse login(String login, String password) throws IOException, InterruptedException {
        return post(
            "/api/v1/auth/login",
            orderedMap(
                "login", login,
                "password", password
            ),
            Map.of()
        );
    }

    protected HttpJsonResponse refresh(String refreshToken) throws IOException, InterruptedException {
        return post("/api/v1/auth/refresh", orderedMap("refreshToken", refreshToken), Map.of());
    }

    protected HttpJsonResponse logout(String accessToken, String refreshToken) throws IOException, InterruptedException {
        return post(
            "/api/v1/auth/logout",
            orderedMap("refreshToken", refreshToken),
            bearerHeaders(accessToken)
        );
    }

    protected HttpJsonResponse changePassword(String accessToken, String oldPassword, String newPassword)
        throws IOException, InterruptedException {
        return post(
            "/api/v1/users/me/change-password",
            orderedMap(
                "oldPassword", oldPassword,
                "newPassword", newPassword
            ),
            bearerHeaders(accessToken)
        );
    }

    protected HttpJsonResponse changeUsername(String accessToken, String newUsername)
        throws IOException, InterruptedException {
        return post(
            "/api/v1/users/me/change-username",
            orderedMap("newUsername", newUsername),
            bearerHeaders(accessToken)
        );
    }

    protected AuthSession registerAndLogin(TestUser user) throws IOException, InterruptedException {
        var registerResponse = register(user);
        assertEquals(201, registerResponse.statusCode(), registerResponse.describe());
        assertEquals(user.email(), registerResponse.body().path("email").asText(), registerResponse.describe());
        assertEquals(user.username(), registerResponse.body().path("username").asText(), registerResponse.describe());
        assertFalse(registerResponse.body().path("id").asText().isBlank(), registerResponse.describe());

        var loginResponse = login(user.username(), user.password());
        assertEquals(200, loginResponse.statusCode(), loginResponse.describe());

        return new AuthSession(
            loginResponse.body().path("accessToken").path("token").asText(),
            loginResponse.body().path("refreshToken").path("token").asText(),
            loginResponse
        );
    }

    protected HttpJsonResponse post(String path, Object payload, Map<String, String> headers)
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

    protected Map<String, Object> orderedMap(Object... pairs) {
        var values = new LinkedHashMap<String, Object>();
        for (var index = 0; index < pairs.length; index += 2) {
            values.put((String) pairs[index], pairs[index + 1]);
        }
        return values;
    }

    private Map<String, String> bearerHeaders(String accessToken) {
        return Map.of("Authorization", "Bearer " + accessToken);
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

    protected record TestUser(String email, String username, String password) {
    }

    protected record AuthSession(String accessToken, String refreshToken, HttpJsonResponse loginResponse) {
    }

    protected record HttpJsonResponse(int statusCode, JsonNode body, String rawBody) {
        public String describe() {
            return "status=" + statusCode + ", body=" + rawBody;
        }
    }
}
