package ttrpg.CharManagementService.domain.user;

import java.time.Instant;

import ttrpg.CharManagementService.domain.exception.ExternalExceptions.InvalidCredentailsException;
import ttrpg.CharManagementService.domain.exception.InternalExceptions.InvalidArgumentException;

public class User {
    private UserId id;

    private String email;
    private String username;
    private String passwordHash;
    private String provider; // Maybe later will be enum
    private String providerId;
    private UserRole role;
    private Instant createdAt;
    private Instant updatedAt;

    public User(String email, String username) {
        this.id = UserId.newId();
        this.email = email;
        this.username = username;
        this.passwordHash = new String();
        this.provider = new String();
        this.providerId = new String();
        this.role = UserRole.USER;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public User(String email, String username, String passwordHash) {
        this.id = UserId.newId();
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.provider = new String();
        this.providerId = new String();
        this.role = UserRole.USER;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public User(UserId id, String email, String username,
                String passwordHash, String provider, String providerId,
                UserRole role, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public final UserId getId() {
        return id;
    }

    public final String getEmail() {
        return email;
    }

    public final String getUsername() {
        return username;
    }

    public final UserRole getRole() {
        return role;
    }

    public final Instant getCreatedAt() {
        return createdAt;
    }

    public final Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isValidPassword(String passwordHash) {
        return this.passwordHash.equals(passwordHash);
    }

    public void setPasswordHash(String oldPasswordHash, String newPasswordHash) {
        if (oldPasswordHash == null || newPasswordHash == null) {
            throw new InvalidArgumentException();
        }
        if (oldPasswordHash.isBlank() || newPasswordHash.isBlank() || !isValidPassword(oldPasswordHash)) {
            throw new InvalidCredentailsException();
        }
        this.passwordHash = newPasswordHash;
    }
}
