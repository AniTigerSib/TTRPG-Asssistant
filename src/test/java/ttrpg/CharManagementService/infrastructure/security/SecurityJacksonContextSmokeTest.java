package ttrpg.CharManagementService.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(SecurityJacksonContextSmokeTest.TestConfiguration.class)
class SecurityJacksonContextSmokeTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void createsSecurityJsonBeansWithBootJacksonObjectMapper() {
        assertNotNull(applicationContext.getBean(ObjectMapper.class));
        assertNotNull(applicationContext.getBean(ApiAuthenticationEntryPoint.class));
        assertNotNull(applicationContext.getBean(ApiAccessDeniedHandler.class));
    }

    @Configuration(proxyBeanMethods = false)
    @Import({
        ApiAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class
    })
    static class TestConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
