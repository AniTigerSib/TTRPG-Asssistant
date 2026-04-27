package ttrpg.CharManagementService.presentation.rest;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.auth.ChangePasswordCommand;
import ttrpg.CharManagementService.application.auth.ChangePasswordUseCase;
import ttrpg.CharManagementService.application.auth.LoginUserCommand;
import ttrpg.CharManagementService.application.auth.LoginUserUseCase;
import ttrpg.CharManagementService.application.auth.RegisterUserCommand;
import ttrpg.CharManagementService.application.auth.RegisterUserUseCase;
import ttrpg.CharManagementService.presentation.dto.ChangePasswordRequest;
import ttrpg.CharManagementService.presentation.dto.LoginUserRequest;
import ttrpg.CharManagementService.presentation.dto.RegisterUserRequest;
import ttrpg.CharManagementService.presentation.dto.UserResponse;
import ttrpg.CharManagementService.presentation.mapper.UserResponseMapper;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UserResponseMapper userResponseMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        var user = registerUserUseCase.execute(
            new RegisterUserCommand(request.email(), request.username(), request.password())
        );
        return userResponseMapper.toUserResponse(user);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse login(@Valid @RequestBody LoginUserRequest request) {
        var user = loginUserUseCase.execute(
            new LoginUserCommand(request.login(), request.password())
        );
        return userResponseMapper.toUserResponse(user);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        changePasswordUseCase.execute(
            new ChangePasswordCommand(request.userId(), request.oldPassword(), request.newPassword())
        );
    }
}
