package ttrpg.CharManagementService.application.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.auth.PasswordHasher;
import ttrpg.CharManagementService.domain.auth.PasswordPolicy;
import ttrpg.CharManagementService.domain.auth.PasswordPolicyContext;
import ttrpg.CharManagementService.domain.exception.InvalidCredentialsException;
import ttrpg.CharManagementService.domain.exception.InvalidPasswordException;
import ttrpg.CharManagementService.domain.exception.UserNotFoundException;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final PasswordHasher passwordHasher;

    @Transactional
    public void execute(ChangePasswordCommand command) {
        Checkers.requireNonNull(command, "command");
        Checkers.requireNonNull(command.userId(), "userId");

        var oldPassword = Checkers.requireStringNonBlank(command.oldPassword(), "oldPassword");
        var newPassword = Checkers.requireStringNonBlank(command.newPassword(), "newPassword");

        var userId = new UserId(command.userId());
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (!passwordHasher.matches(oldPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is invalid");
        }

        if (passwordHasher.matches(newPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("New password must differ from current password", "newPassword");
        }

        passwordPolicy.validate(newPassword, new PasswordPolicyContext(user.getEmail(), user.getUsername()));

        user.changePasswordHash(passwordHasher.hash(newPassword));
        userRepository.save(user);
    }
}
