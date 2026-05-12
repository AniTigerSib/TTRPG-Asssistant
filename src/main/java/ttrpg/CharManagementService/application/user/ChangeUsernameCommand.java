package ttrpg.CharManagementService.application.user;

import java.util.UUID;

public record ChangeUsernameCommand(
    UUID userId,
    String newUsername
) {}
