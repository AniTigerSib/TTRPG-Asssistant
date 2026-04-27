package ttrpg.CharManagementService.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.auth.PasswordHasher;
import ttrpg.CharManagementService.domain.auth.PasswordPolicy;
import ttrpg.CharManagementService.domain.auth.PasswordPolicyContext;
import ttrpg.CharManagementService.domain.exception.ExternalExceptions.UserAlreadyExistsException;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final PasswordHasher passwordHasher;

    @Transactional
    public User execute(RegisterUserCommand command) {
        Checkers.requireNonNull(command, "command");

        var email = Checkers.requireStringNonBlank(command.email(), "email");
        var username = Checkers.requireStringNonBlank(command.username(), "username");
        var password = Checkers.requireStringNonBlank(command.password(), "password");

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already registered");
        }
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        passwordPolicy.validate(password, new PasswordPolicyContext(email, username));

        var passwordHash = passwordHasher.hash(password);
        var user = User.create(email, username, passwordHash);

        return userRepository.save(user);
    }
}
