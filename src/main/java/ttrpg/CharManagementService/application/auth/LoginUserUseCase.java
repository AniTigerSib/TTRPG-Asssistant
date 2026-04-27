package ttrpg.CharManagementService.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.auth.PasswordHasher;
import ttrpg.CharManagementService.domain.exception.ExternalExceptions.InvalidCredentailsException;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional(readOnly = true)
    public User execute(LoginUserCommand command) {
        Checkers.requireNonNull(command, "command");

        var login = Checkers.requireStringNonBlank(command.login(), "login");
        var password = Checkers.requireStringNonBlank(command.password(), "password");

        var user = findByLogin(login);
        if (user == null || !passwordHasher.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentailsException("Invalid login or password");
        }

        return user;
    }

    private User findByLogin(String login) {
        return userRepository.findByEmail(login)
            .or(() -> userRepository.findByUsername(login))
            .orElse(null);
    }
}
