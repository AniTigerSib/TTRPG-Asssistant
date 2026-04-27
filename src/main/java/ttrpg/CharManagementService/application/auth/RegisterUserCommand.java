package ttrpg.CharManagementService.application.auth;

public record RegisterUserCommand(
    String email,
    String username,
    String password
) {}
