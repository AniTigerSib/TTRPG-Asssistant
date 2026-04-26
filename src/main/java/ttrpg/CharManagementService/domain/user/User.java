package ttrpg.CharManagementService.domain.user;

import java.time.Instant;

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
}
