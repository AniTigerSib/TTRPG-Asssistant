package ttrpg.CharManagementService.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.UserId;

@Service
@RequiredArgsConstructor
public class LogoutUserUseCase {

    private final AuthenticationTokenService authenticationTokenService;

    @Transactional
    public void execute(LogoutUserCommand command) {
        Checkers.requireNonNull(command, "command");
        Checkers.requireNonNull(command.userId(), "userId");

        authenticationTokenService.logout(
            new UserId(command.userId()),
            Checkers.requireStringNonBlank(command.accessToken(), "accessToken"),
            Checkers.requireStringNonBlank(command.refreshToken(), "refreshToken")
        );
    }
}
