package ttrpg.CharManagementService.domain.auth;

public record PasswordPolicyContext(
    String email,
    String username
) {}
