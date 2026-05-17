package ttrpg.CharManagementService.presentation.rest;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.user.ChangePasswordCommand;
import ttrpg.CharManagementService.application.user.ChangePasswordUseCase;
import ttrpg.CharManagementService.application.user.ChangeUsernameCommand;
import ttrpg.CharManagementService.application.user.ChangeUsernameUseCase;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.presentation.dto.ChangePasswordRequest;
import ttrpg.CharManagementService.presentation.dto.ChangeUsernameRequest;
import ttrpg.CharManagementService.presentation.dto.UserResponse;
import ttrpg.CharManagementService.presentation.mapper.UserResponseMapper;

@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final ChangePasswordUseCase changePasswordUseCase;
    private final ChangeUsernameUseCase changeUsernameUseCase;
    private final UserResponseMapper userResponseMapper;

    @PostMapping("/me/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        changePasswordUseCase.execute(
            new ChangePasswordCommand(currentUser.getId().value(), request.oldPassword(), request.newPassword())
        );
    }

    @PostMapping("/me/change-username")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse changeUsername(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody ChangeUsernameRequest request
    ) {
        var user = changeUsernameUseCase.execute(
            new ChangeUsernameCommand(currentUser.getId().value(), request.newUsername())
        );
        return userResponseMapper.toUserResponse(user);
    }
}
