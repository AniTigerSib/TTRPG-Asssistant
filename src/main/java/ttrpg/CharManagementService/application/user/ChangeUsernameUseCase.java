package ttrpg.CharManagementService.application.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.exception.UserAlreadyExistsException;
import ttrpg.CharManagementService.domain.exception.UserNotFoundException;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class ChangeUsernameUseCase {

    private final UserRepository userRepository;

    @Transactional
    public User execute(ChangeUsernameCommand command) {
        Checkers.requireNonNull(command, "command");
        Checkers.requireNonNull(command.userId(), "userId");

        var newUsername = Checkers.requireStringNonBlank(command.newUsername(), "newUsername");
        var userId = new UserId(command.userId());
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (user.getUsername().equals(newUsername)) {
            return user;
        }

        if (!user.getUsername().equalsIgnoreCase(newUsername) && userRepository.existsByUsername(newUsername)) {
            throw UserAlreadyExistsException.username(newUsername);
        }

        user.changeUsername(newUsername);
        return userRepository.save(user);
    }
}
