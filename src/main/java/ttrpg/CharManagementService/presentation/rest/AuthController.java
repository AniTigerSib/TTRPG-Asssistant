package ttrpg.CharManagementService.presentation.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.auth.LoginUserCommand;
import ttrpg.CharManagementService.application.auth.LoginUserUseCase;
import ttrpg.CharManagementService.application.auth.LogoutUserCommand;
import ttrpg.CharManagementService.application.auth.LogoutUserUseCase;
import ttrpg.CharManagementService.application.auth.RefreshAuthenticationCommand;
import ttrpg.CharManagementService.application.auth.RefreshAuthenticationUseCase;
import ttrpg.CharManagementService.application.auth.RegisterUserCommand;
import ttrpg.CharManagementService.application.auth.RegisterUserUseCase;
import ttrpg.CharManagementService.domain.exception.InvariantViolationException;
import ttrpg.CharManagementService.domain.exception.InvalidTokenException;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.presentation.dto.AuthenticationResponse;
import ttrpg.CharManagementService.presentation.dto.LoginUserRequest;
import ttrpg.CharManagementService.presentation.dto.LogoutRequest;
import ttrpg.CharManagementService.presentation.dto.RefreshTokenRequest;
import ttrpg.CharManagementService.presentation.dto.RegisterUserRequest;
import ttrpg.CharManagementService.presentation.dto.UserResponse;
import ttrpg.CharManagementService.presentation.mapper.AuthenticationResponseMapper;
import ttrpg.CharManagementService.presentation.mapper.UserResponseMapper;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshAuthenticationUseCase refreshAuthenticationUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final UserResponseMapper userResponseMapper;
    private final AuthenticationResponseMapper authenticationResponseMapper;

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
    public AuthenticationResponse login(
        @Valid @RequestBody LoginUserRequest request,
        HttpServletRequest httpServletRequest
    ) {
        var authenticationResult = loginUserUseCase.execute(
            new LoginUserCommand(
                request.login(),
                request.password(),
                resolveUserAgent(httpServletRequest),
                resolveIpAddress(httpServletRequest)
            )
        );
        return authenticationResponseMapper.toResponse(authenticationResult);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationResponse refresh(
        @Valid @RequestBody RefreshTokenRequest request,
        HttpServletRequest httpServletRequest
    ) {
        var authenticationResult = refreshAuthenticationUseCase.execute(
            new RefreshAuthenticationCommand(
                request.refreshToken(),
                resolveUserAgent(httpServletRequest),
                resolveIpAddress(httpServletRequest)
            )
        );
        return authenticationResponseMapper.toResponse(authenticationResult);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody LogoutRequest request,
        HttpServletRequest httpServletRequest
    ) {
        logoutUserUseCase.execute(
            new LogoutUserCommand(
                currentUser.getId().value(),
                extractBearerToken(httpServletRequest),
                request.refreshToken()
            )
        );
    }

    private String resolveUserAgent(HttpServletRequest request) {
        var userAgent = request.getHeader("User-Agent");
        return userAgent == null || userAgent.isBlank() ? "unknown" : userAgent;
    }

    private InetAddress resolveIpAddress(HttpServletRequest request) {
        try {
            return InetAddress.getByName(request.getRemoteAddr());
        } catch (UnknownHostException exception) {
            throw new InvariantViolationException("Failed to resolve request IP address", exception);
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        var authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            throw new InvalidTokenException("Missing bearer access token");
        }
        return authorization.substring(7).trim();
    }
}
