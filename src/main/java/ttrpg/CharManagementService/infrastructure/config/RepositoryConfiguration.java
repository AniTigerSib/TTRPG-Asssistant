package ttrpg.CharManagementService.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = "ttrpg.CharManagementService.infrastructure.persistence.repository")
public class RepositoryConfiguration {
}
