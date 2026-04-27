package ttrpg.CharManagementService.infrastructure.security;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.auth.PasswordPolicy;
import ttrpg.CharManagementService.domain.auth.PasswordPolicyContext;
import ttrpg.CharManagementService.domain.exception.ExternalExceptions.InvalidPasswordException;
import ttrpg.CharManagementService.domain.shared.Checkers;

@Component
public class DefaultPasswordPolicy implements PasswordPolicy {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 72;

    @Override
    public void validate(String rawPassword, PasswordPolicyContext context) {
        var password = Checkers.requireStringNonBlank(rawPassword, "password");

        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new InvalidPasswordException(
                "Password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters"
            );
        }

        if (!containsUppercase(password)) {
            throw new InvalidPasswordException("Password must contain at least one uppercase letter");
        }
        if (!containsLowercase(password)) {
            throw new InvalidPasswordException("Password must contain at least one lowercase letter");
        }
        if (!containsDigit(password)) {
            throw new InvalidPasswordException("Password must contain at least one digit");
        }

        if (context != null) {
            validateAgainstIdentity(password, context.email(), "email");
            validateAgainstIdentity(password, context.username(), "username");
        }
    }

    private void validateAgainstIdentity(String password, String identityValue, String fieldName) {
        if (identityValue != null && !identityValue.isBlank() && password.equalsIgnoreCase(identityValue)) {
            throw new InvalidPasswordException("Password must not match " + fieldName);
        }
    }

    private boolean containsUppercase(String value) {
        return value.chars().anyMatch(Character::isUpperCase);
    }

    private boolean containsLowercase(String value) {
        return value.chars().anyMatch(Character::isLowerCase);
    }

    private boolean containsDigit(String value) {
        return value.chars().anyMatch(Character::isDigit);
    }
}
