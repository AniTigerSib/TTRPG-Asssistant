package ttrpg.CharManagementService.application.auth;

import java.util.UUID;

public record ChangePasswordCommand(
    UUID userId,
    String oldPassword,
    String newPassword
) {}
