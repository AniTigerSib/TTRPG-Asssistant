package ttrpg.CharManagementService.application.auth;

public record LoginUserCommand(
    String login,
    String password
) {}
