package ttrpg.CharManagementService.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig
@ContextConfiguration(classes = SecurityJacksonContextSmokeTest.TestConfiguration.class)
class SecurityJacksonContextSmokeTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void createsSecurityJsonBeansWithBootJacksonObjectMapper() {
        assertNotNull(applicationContext.getBean(ObjectMapper.class));
        assertNotNull(applicationContext.getBean(ApiAuthenticationEntryPoint.class));
        assertNotNull(applicationContext.getBean(ApiAccessDeniedHandler.class));
    }

    @Import({
        JacksonAutoConfiguration.class,
        ApiAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class
    })
    static class TestConfiguration {
    }
}
