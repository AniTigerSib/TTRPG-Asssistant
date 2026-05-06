package ttrpg.CharManagementService.infrastructure.security.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.auth.tokens")
public record AuthTokenProperties(
    @NotNull Duration accessTtl,
    @NotNull Duration refreshTtl
) {}
