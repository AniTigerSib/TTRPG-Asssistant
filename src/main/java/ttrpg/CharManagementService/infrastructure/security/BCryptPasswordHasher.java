package ttrpg.CharManagementService.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.auth.PasswordHasher;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return delegate.matches(rawPassword, hashedPassword);
    }
}
