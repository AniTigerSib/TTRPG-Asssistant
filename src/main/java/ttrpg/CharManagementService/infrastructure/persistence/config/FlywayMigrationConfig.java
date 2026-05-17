package ttrpg.CharManagementService.infrastructure.persistence.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class FlywayMigrationConfig {

    @Bean(name = "flyway", initMethod = "migrate")
    Flyway flyway(
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.flyway.user}") String user,
            @Value("${spring.flyway.password}") String password,
            @Value("${spring.flyway.default-schema}") String defaultSchema,
            @Value("${spring.flyway.locations}") String locations) {
        return Flyway.configure()
                .dataSource(jdbcUrl, user, password)
                .defaultSchema(defaultSchema)
                .schemas(defaultSchema)
                .locations(locations)
                .baselineOnMigrate(true)
                .load();
    }

    @Configuration(proxyBeanMethods = false)
    static class FlywayEntityManagerFactoryDependencyConfig extends EntityManagerFactoryDependsOnPostProcessor {
        FlywayEntityManagerFactoryDependencyConfig() {
            super("flyway");
        }
    }
}
