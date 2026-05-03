package ttrpg.CharManagementService.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.auth.PasswordPolicyContext;
import ttrpg.CharManagementService.domain.exception.InvalidPasswordException;

class DefaultPasswordPolicyTest {

    private final DefaultPasswordPolicy policy = new DefaultPasswordPolicy();

    @Test
    void acceptsStrongPassword() {
        assertDoesNotThrow(() -> policy.validate("StrongPass1", new PasswordPolicyContext("test@example.com", "hero")));
    }

    @Test
    void rejectsShortPassword() {
        assertThrows(
            InvalidPasswordException.class,
            () -> policy.validate("Short1", new PasswordPolicyContext("test@example.com", "hero"))
        );
    }

    @Test
    void rejectsPasswordMatchingUsername() {
        assertThrows(
            InvalidPasswordException.class,
            () -> policy.validate("Hero", new PasswordPolicyContext("test@example.com", "Hero"))
        );
    }
}
