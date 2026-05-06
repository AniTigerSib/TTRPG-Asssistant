package ttrpg.CharManagementService.infrastructure.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app.redis")
public record RedisProperties(
    @NotBlank String host,
    @Min(1) @Max(65535) int port,
    String password
) {}
