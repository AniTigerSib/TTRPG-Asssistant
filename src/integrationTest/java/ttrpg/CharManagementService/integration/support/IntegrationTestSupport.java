package ttrpg.CharManagementService.integration.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.NullNode;

public abstract class IntegrationTestSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = System.getProperty(
        "integration.base-url",
        System.getenv().getOrDefault("INTEGRATION_BASE_URL", "http://localhost:8080")
    );
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "ttrpg_db");
    private static final String DB_HOST = resolveDbHost(System.getenv().getOrDefault("DB_HOST", "localhost"));
    private static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "5432");
    private static final String DB_SCHEMA = System.getenv().getOrDefault("DB_SCHEMA", "ttrpg_assistant_app");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "ttrpg_app_user");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "ttrpg_app_pass");

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

    protected HttpJsonResponse get(String path, Map<String, String> headers) throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .header("Accept", "application/json");
        headers.forEach(requestBuilder::header);
        return send(requestBuilder.GET().build());
    }

    protected HttpJsonResponse put(String path, Object payload, Map<String, String> headers)
        throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json");
        headers.forEach(requestBuilder::header);
        return send(
            requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(payload))).build()
        );
    }

    protected Map<String, String> bearer(String accessToken) {
        return bearerHeaders(accessToken);
    }

    protected void grantUserRole(UUID userId, String role) throws SQLException {
        executeUpdate(
            "insert into " + DB_SCHEMA + ".user_roles(user_id, role) values (?, ?) on conflict do nothing",
            statement -> {
                statement.setObject(1, userId);
                statement.setString(2, role);
            }
        );
    }

    protected UUID createCampaign(UUID ownerId, UUID gameSystemId, String name, String visibility) throws SQLException {
        var campaignId = UUID.randomUUID();
        executeUpdate(
            """
            insert into %s.campaigns(id, owner_id, game_system_id, name, description, visibility, created_at, updated_at)
            values (?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(DB_SCHEMA),
            statement -> {
                statement.setObject(1, campaignId);
                statement.setObject(2, ownerId);
                statement.setObject(3, gameSystemId);
                statement.setString(4, name);
                statement.setString(5, null);
                statement.setString(6, visibility);
                statement.setObject(7, Instant.now());
                statement.setObject(8, Instant.now());
            }
        );
        return campaignId;
    }

    protected void addCampaignMember(UUID campaignId, UUID userId, String role) throws SQLException {
        executeUpdate(
            "insert into " + DB_SCHEMA + ".campaign_members(campaign_id, user_id, role) values (?, ?, ?)",
            statement -> {
                statement.setObject(1, campaignId);
                statement.setObject(2, userId);
                statement.setString(3, role);
            }
        );
    }

    protected UUID findGameSystemIdByCode(String code) throws SQLException {
        try (var connection = openConnection();
             var statement = connection.prepareStatement(
                 "select id from " + DB_SCHEMA + ".game_systems where upper(code) = upper(?)"
             )) {
            statement.setString(1, code);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Game system not found for code: " + code);
                }
                return resultSet.getObject(1, UUID.class);
            }
        }
    }

    protected UUID findTemplateIdByName(String name) throws SQLException {
        try (var connection = openConnection();
             var statement = connection.prepareStatement(
                 "select id from " + DB_SCHEMA + ".character_templates where name = ?"
             )) {
            statement.setString(1, name);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Template not found for name: " + name);
                }
                return resultSet.getObject(1, UUID.class);
            }
        }
    }

    protected void insertTemplate(
        UUID templateId,
        UUID gameSystemId,
        String name,
        String schemaJson,
        int version,
        boolean official,
        String visibility
    ) throws SQLException {
        executeUpdate(
            """
            insert into %s.character_templates(id, game_system_id, name, schema, version, is_official, visibility, created_at)
            values (?, ?, ?, cast(? as jsonb), ?, ?, ?, ?)
            """.formatted(DB_SCHEMA),
            statement -> {
                statement.setObject(1, templateId);
                statement.setObject(2, gameSystemId);
                statement.setString(3, name);
                statement.setString(4, schemaJson);
                statement.setInt(5, version);
                statement.setBoolean(6, official);
                statement.setString(7, visibility);
                statement.setObject(8, Instant.now());
            }
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

    private void executeUpdate(String sql, SqlConsumer<PreparedStatement> binder) throws SQLException {
        try (var connection = openConnection(); var statement = connection.prepareStatement(sql)) {
            binder.accept(statement);
            statement.executeUpdate();
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?currentSchema=" + DB_SCHEMA,
            DB_USER,
            DB_PASSWORD
        );
    }

    private static String resolveDbHost(String rawHost) {
        return "postgres".equalsIgnoreCase(rawHost) ? "localhost" : rawHost;
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

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T value) throws SQLException;
    }
}
