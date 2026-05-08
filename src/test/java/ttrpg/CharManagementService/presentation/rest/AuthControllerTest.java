package ttrpg.CharManagementService.presentation.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ttrpg.CharManagementService.application.auth.AuthenticationResult;
import ttrpg.CharManagementService.application.auth.AuthenticationTokens;
import ttrpg.CharManagementService.application.auth.ChangePasswordUseCase;
import ttrpg.CharManagementService.application.auth.LoginUserCommand;
import ttrpg.CharManagementService.application.auth.LoginUserUseCase;
import ttrpg.CharManagementService.application.auth.LogoutUserUseCase;
import ttrpg.CharManagementService.application.auth.RefreshAuthenticationCommand;
import ttrpg.CharManagementService.application.auth.RefreshAuthenticationUseCase;
import ttrpg.CharManagementService.application.auth.RegisterUserUseCase;
import ttrpg.CharManagementService.domain.exception.InvalidTokenException;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.presentation.dto.AuthenticationResponse;
import ttrpg.CharManagementService.presentation.dto.TokenResponse;
import ttrpg.CharManagementService.presentation.dto.UserResponse;
import ttrpg.CharManagementService.presentation.errors.GlobalExceptionHandler;
import ttrpg.CharManagementService.presentation.mapper.AuthenticationResponseMapper;
import ttrpg.CharManagementService.presentation.mapper.UserResponseMapper;

class AuthControllerTest {

    private final RegisterUserUseCase registerUserUseCase = Mockito.mock(RegisterUserUseCase.class);
    private final LoginUserUseCase loginUserUseCase = Mockito.mock(LoginUserUseCase.class);
    private final RefreshAuthenticationUseCase refreshAuthenticationUseCase = Mockito.mock(RefreshAuthenticationUseCase.class);
    private final ChangePasswordUseCase changePasswordUseCase = Mockito.mock(ChangePasswordUseCase.class);
    private final LogoutUserUseCase logoutUserUseCase = Mockito.mock(LogoutUserUseCase.class);
    private final UserResponseMapper userResponseMapper = Mockito.mock(UserResponseMapper.class);
    private final AuthenticationResponseMapper authenticationResponseMapper = Mockito.mock(AuthenticationResponseMapper.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(
                    registerUserUseCase,
                    loginUserUseCase,
                    refreshAuthenticationUseCase,
                    changePasswordUseCase,
                    logoutUserUseCase,
                    userResponseMapper,
                    authenticationResponseMapper
                )
            )
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void returnsTokensOnLogin() throws Exception {
        var result = authenticationResult();
        var response = authenticationResponse(result);
        when(loginUserUseCase.execute(any(LoginUserCommand.class))).thenReturn(result);
        when(authenticationResponseMapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(APPLICATION_JSON)
                .header("User-Agent", "JUnit-Agent")
                .with(request -> {
                    request.setRemoteAddr("127.0.0.10");
                    return request;
                })
                .content("""
                    {
                      "login": "hero",
                      "password": "Password1"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.username").value("hero"))
            .andExpect(jsonPath("$.accessToken.token").value("access-token"))
            .andExpect(jsonPath("$.refreshToken.token").value("refresh-token"));

        var captor = ArgumentCaptor.forClass(LoginUserCommand.class);
        verify(loginUserUseCase).execute(captor.capture());
        assertEquals("hero", captor.getValue().login());
        assertEquals("JUnit-Agent", captor.getValue().userAgent());
        assertEquals(InetAddress.getByName("127.0.0.10"), captor.getValue().ipAddress());
    }

    @Test
    void returnsTokensOnRefresh() throws Exception {
        var result = authenticationResult();
        var response = authenticationResponse(result);
        when(refreshAuthenticationUseCase.execute(any(RefreshAuthenticationCommand.class))).thenReturn(result);
        when(authenticationResponseMapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(APPLICATION_JSON)
                .header("User-Agent", "JUnit-Agent")
                .content("""
                    {
                      "refreshToken": "refresh-token"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken.expiresAt").value("2026-05-06T10:15:30Z"))
            .andExpect(jsonPath("$.refreshToken.expiresAt").value("2026-06-05T10:15:30Z"));
    }

    @Test
    void mapsInvalidRefreshTokenToUnauthorized() throws Exception {
        when(refreshAuthenticationUseCase.execute(any(RefreshAuthenticationCommand.class)))
            .thenThrow(new InvalidTokenException("Invalid refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": "bad-token"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    private AuthenticationResult authenticationResult() {
        var user = User.create("test@example.com", "hero", "hashed:Password1");
        return new AuthenticationResult(
            user,
            new AuthenticationTokens(
                "access-token",
                Instant.parse("2026-05-06T10:15:30Z"),
                "refresh-token",
                Instant.parse("2026-06-05T10:15:30Z")
            )
        );
    }

    private AuthenticationResponse authenticationResponse(AuthenticationResult result) {
        return new AuthenticationResponse(
            new UserResponse(
                result.user().getId().value(),
                result.user().getEmail(),
                result.user().getUsername(),
                Set.of("USER"),
                result.user().getCreatedAt(),
                result.user().getUpdatedAt()
            ),
            new TokenResponse(result.tokens().accessToken(), result.tokens().accessTokenExpiresAt()),
            new TokenResponse(result.tokens().refreshToken(), result.tokens().refreshTokenExpiresAt())
        );
    }
}
