package ttrpg.CharManagementService.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.exception.UserNotFoundException;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class RefreshAuthenticationUseCase {

    private final AuthenticationTokenService authenticationTokenService;
    private final UserRepository userRepository;

    @Transactional
    public AuthenticationResult execute(RefreshAuthenticationCommand command) {
        Checkers.requireNonNull(command, "command");

        var userAgent = Checkers.requireStringNonBlank(command.userAgent(), "userAgent");
        var ipAddress = Checkers.requireNonNull(command.ipAddress(), "ipAddress");
        var refreshToken = Checkers.requireStringNonBlank(command.refreshToken(), "refreshToken");

        var refreshedTokens = authenticationTokenService.refreshTokens(refreshToken, userAgent, ipAddress);
        var user = userRepository.findById(refreshedTokens.userId())
            .orElseThrow(() -> new UserNotFoundException(refreshedTokens.userId().value()));

        return new AuthenticationResult(user, refreshedTokens.tokens());
    }
}
