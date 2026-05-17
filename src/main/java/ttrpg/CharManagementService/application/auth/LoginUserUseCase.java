package ttrpg.CharManagementService.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.auth.PasswordHasher;
import ttrpg.CharManagementService.domain.exception.InvalidCredentialsException;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthenticationTokenService authenticationTokenService;

    @Transactional
    public AuthenticationResult execute(LoginUserCommand command) {
        Checkers.requireNonNull(command, "command");

        var login = Checkers.requireStringNonBlank(command.login(), "login");
        var password = Checkers.requireStringNonBlank(command.password(), "password");
        var userAgent = Checkers.requireStringNonBlank(command.userAgent(), "userAgent");
        var ipAddress = Checkers.requireNonNull(command.ipAddress(), "ipAddress");

        var user = findByLogin(login);
        if (user == null || !passwordHasher.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid login or password");
        }

        var tokens = authenticationTokenService.issueTokens(user.getId(), userAgent, ipAddress);
        return new AuthenticationResult(user, tokens);
    }

    private User findByLogin(String login) {
        return userRepository.findByEmail(login)
            .or(() -> userRepository.findByUsername(login))
            .orElse(null);
    }
}
