package ttrpg.CharManagementService.infrastructure.security;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ttrpg.CharManagementService.application.auth.AuthenticationTokenService;
import ttrpg.CharManagementService.infrastructure.config.JacksonConfig;
import ttrpg.CharManagementService.domain.user.UserRepository;

@SpringBootTest(
    classes = OpenApiSecurityConfigTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.autoconfigure.exclude="
            + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
            + "org.springframework.boot.data.jpa.autoconfigure.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration,"
            + "org.springframework.boot.data.redis.autoconfigure.RedisAutoConfiguration,"
            + "org.springframework.boot.data.redis.autoconfigure.RedisRepositoriesAutoConfiguration,"
            + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class OpenApiSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsOpenApiJsonWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                {"openapi":"3.1.0"}
                """));
    }

    @Test
    void permitsOpenApiYamlWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs.yaml"))
            .andExpect(status().isOk())
            .andExpect(content().string("openapi: 3.1.0"));
    }

    @Test
    void permitsSwaggerUiWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().string("swagger-ui"));
    }

    @Test
    void keepsOtherEndpointsProtected() throws Exception {
        mockMvc.perform(get("/api/v1/secured-probe"))
            .andExpect(status().isUnauthorized());
    }

    @RestController
    static class DocsProbeController {

        @GetMapping("/v3/api-docs")
        Map<String, String> apiDocs() {
            return Map.of("openapi", "3.1.0");
        }

        @GetMapping("/v3/api-docs.yaml")
        String apiDocsYaml() {
            return "openapi: 3.1.0";
        }

        @GetMapping("/swagger-ui/index.html")
        String swaggerUiIndex() {
            return "swagger-ui";
        }

        @GetMapping("/api/v1/secured-probe")
        String securedProbe() {
            return "secured";
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestSecurityBeans {

        @Bean
        AuthenticationTokenService authenticationTokenService() {
            return mock(AuthenticationTokenService.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        AccessTokenAuthenticationFilter accessTokenAuthenticationFilter(
            AuthenticationTokenService authenticationTokenService,
            UserRepository userRepository
        ) {
            return new AccessTokenAuthenticationFilter(authenticationTokenService, userRepository);
        }

        @Bean
        ApiAuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper objectMapper) {
            return new ApiAuthenticationEntryPoint(objectMapper);
        }

        @Bean
        ApiAccessDeniedHandler apiAccessDeniedHandler(ObjectMapper objectMapper) {
            return new ApiAccessDeniedHandler(objectMapper);
        }
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import({
        SecurityConfig.class,
        JacksonConfig.class,
        DocsProbeController.class,
        TestSecurityBeans.class
    })
    static class TestApplication {
    }
}
