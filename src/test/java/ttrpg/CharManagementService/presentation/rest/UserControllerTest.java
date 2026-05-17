package ttrpg.CharManagementService.presentation.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ttrpg.CharManagementService.application.user.ChangePasswordCommand;
import ttrpg.CharManagementService.application.user.ChangePasswordUseCase;
import ttrpg.CharManagementService.application.user.ChangeUsernameCommand;
import ttrpg.CharManagementService.application.user.ChangeUsernameUseCase;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.presentation.dto.UserResponse;
import ttrpg.CharManagementService.presentation.errors.GlobalExceptionHandler;
import ttrpg.CharManagementService.presentation.mapper.UserResponseMapper;

class UserControllerTest {

    private final ChangePasswordUseCase changePasswordUseCase = Mockito.mock(ChangePasswordUseCase.class);
    private final ChangeUsernameUseCase changeUsernameUseCase = Mockito.mock(ChangeUsernameUseCase.class);
    private final UserResponseMapper userResponseMapper = Mockito.mock(UserResponseMapper.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new UserController(changePasswordUseCase, changeUsernameUseCase, userResponseMapper)
            )
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void changesPasswordForAuthenticatedUser() throws Exception {
        var currentUser = authenticatedUser("hero");
        setCurrentUser(currentUser);

        mockMvc.perform(post("/api/v1/users/me/change-password")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "oldPassword": "Password1",
                      "newPassword": "BetterPass2"
                    }
                    """))
            .andExpect(status().isNoContent());

        var captor = ArgumentCaptor.forClass(ChangePasswordCommand.class);
        verify(changePasswordUseCase).execute(captor.capture());
        assertEquals(currentUser.getId().value(), captor.getValue().userId());
        assertEquals("Password1", captor.getValue().oldPassword());
        assertEquals("BetterPass2", captor.getValue().newPassword());
    }

    @Test
    void returnsUpdatedUserOnUsernameChange() throws Exception {
        var currentUser = authenticatedUser("hero");
        var updatedUser = User.create("test@example.com", "legend", "hashed:Password1");
        var response = new UserResponse(
            updatedUser.getId().value(),
            updatedUser.getEmail(),
            updatedUser.getUsername(),
            java.util.Set.of("USER"),
            updatedUser.getCreatedAt(),
            updatedUser.getUpdatedAt()
        );
        setCurrentUser(currentUser);

        when(changeUsernameUseCase.execute(any(ChangeUsernameCommand.class))).thenReturn(updatedUser);
        when(userResponseMapper.toUserResponse(updatedUser)).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/me/change-username")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "newUsername": "legend"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("legend"));

        var captor = ArgumentCaptor.forClass(ChangeUsernameCommand.class);
        verify(changeUsernameUseCase).execute(captor.capture());
        assertEquals(currentUser.getId().value(), captor.getValue().userId());
        assertEquals("legend", captor.getValue().newUsername());
    }

    private User authenticatedUser(String username) {
        return User.create("test@example.com", username, "hashed:Password1");
    }

    private void setCurrentUser(User currentUser) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null, List.of()));
        SecurityContextHolder.setContext(context);
    }
}
