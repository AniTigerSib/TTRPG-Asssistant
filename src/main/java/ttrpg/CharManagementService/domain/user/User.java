package ttrpg.CharManagementService.domain.user;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ttrpg.CharManagementService.domain.exception.BusinessRuleViolationException;
import ttrpg.CharManagementService.domain.shared.Checkers;

public class User {
    private UserId id;

    private String email;
    private String username;
    private String passwordHash;
    // private String provider; // Maybe later will be enum
    // private String providerId;
    private Set<UserRole> roles;
    private Instant createdAt;
    private Instant updatedAt;

    private User(UserId id, String email, String username,
                String passwordHash, Set<UserRole> roles,
                Instant createdAt, Instant updatedAt) {
        this.id = Checkers.requireNonNull(id, "id");
        this.email = Checkers.requireStringNonBlank(email, "email");
        this.username = Checkers.requireStringNonBlank(username, "username");
        this.passwordHash = Checkers.requireStringNonBlank(passwordHash, "passwordHash");
        this.roles = normalizeRoles(roles);
        this.createdAt = Checkers.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Checkers.requireNonNull(updatedAt, "updatedAt");
    }

    public static User create(String email, String username, String passwordHash) {
        return new User(
            UserId.newId(),
            email,
            username,
            passwordHash,
            new HashSet<>(Arrays.asList(UserRole.USER)),
            Instant.now(),
            Instant.now()
        );
    }

    public static User restore(UserSnapshot snapshot) {
        Checkers.requireNonNull(snapshot, "snapshot");
        return new User(
            snapshot.id(),
            snapshot.email(),
            snapshot.username(),
            snapshot.passwordHash(),
            snapshot.roles(),
            snapshot.createdAt(),
            snapshot.updatedAt()
        );
    }

    public UserId getId() { return id; }

    public String getEmail() { return email; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public Set<UserRole> getRoles() { return Set.copyOf(roles); }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public UserSnapshot snapshot() {
        return new UserSnapshot(
            id,
            email,
            username,
            passwordHash,
            Set.copyOf(roles),
            createdAt,
            updatedAt
        );
    }

    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = Checkers.requireStringNonBlank(newPasswordHash, "newPasswordHash");
        updatedAt = Instant.now();
    }

    public void changeUsername(String newUsername) {
        this.username = Checkers.requireStringNonBlank(newUsername, "newUsername");
        updatedAt = Instant.now();
    }

    public void addRole(UserRole role) {
        roles.add(Checkers.requireNonNull(role, "role"));
        updatedAt = Instant.now();
    }

    public void removeRole(UserRole role) {
        Checkers.requireNonNull(role, "role");
        if (role == UserRole.USER) {
            throw new BusinessRuleViolationException(
                "Base USER role cannot be removed",
                Map.of("role", "Base USER role cannot be removed")
            );
        }
        roles.remove(role);
        updatedAt = Instant.now();
    }

    private static Set<UserRole> normalizeRoles(Set<UserRole> roles) {
        return (roles == null || roles.isEmpty())
                ? new HashSet<>(Arrays.asList(UserRole.USER))
                : new HashSet<>(roles);
    }
}
