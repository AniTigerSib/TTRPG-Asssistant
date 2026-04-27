package ttrpg.CharManagementService.domain.auth;

public interface PasswordPolicy {
    void validate(String rawPassword, PasswordPolicyContext context);
}
