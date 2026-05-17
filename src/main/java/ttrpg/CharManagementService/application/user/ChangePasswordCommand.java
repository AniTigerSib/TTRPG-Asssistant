package ttrpg.CharManagementService.application.user;

import java.util.UUID;

public record ChangePasswordCommand(
    UUID userId,
    String oldPassword,
    String newPassword
) {}
