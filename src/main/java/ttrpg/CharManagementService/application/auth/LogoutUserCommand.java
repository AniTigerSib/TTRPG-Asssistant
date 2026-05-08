package ttrpg.CharManagementService.application.auth;

import java.util.UUID;

public record LogoutUserCommand(
    UUID userId,
    String accessToken,
    String refreshToken
) {}
