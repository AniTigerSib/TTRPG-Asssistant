package ttrpg.CharManagementService.presentation.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

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

import ttrpg.CharManagementService.application.dice.DiceRollResult;
import ttrpg.CharManagementService.application.dice.DiceTermKind;
import ttrpg.CharManagementService.application.dice.DiceTermResult;
import ttrpg.CharManagementService.application.dice.RollDiceCommand;
import ttrpg.CharManagementService.application.dice.RollDiceUseCase;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.presentation.errors.GlobalExceptionHandler;
import ttrpg.CharManagementService.presentation.mapper.DiceRollResponseMapper;

class DiceRollControllerTest {

    private final RollDiceUseCase rollDiceUseCase = Mockito.mock(RollDiceUseCase.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DiceRollController(rollDiceUseCase, new DiceRollResponseMapper()))
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rollsGeneralFormulaForAuthenticatedUser() throws Exception {
        setCurrentUser(authenticatedUser("roller"));
        when(rollDiceUseCase.execute(any(User.class), any(RollDiceCommand.class)))
            .thenReturn(
                new DiceRollResult(
                    null,
                    "2d6 + 2",
                    "2d6 + 2",
                    9,
                    List.of(
                        new DiceTermResult(DiceTermKind.DICE, "2d6", "2d6", 1, List.of(3, 4), 7),
                        new DiceTermResult(DiceTermKind.CONSTANT, "2", "2", 1, List.of(), 2)
                    )
                )
            );

        mockMvc.perform(post("/api/v1/rolls")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "formula": "2d6 + 2"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(9))
            .andExpect(jsonPath("$.terms[0].rolls[1]").value(4));
    }

    @Test
    void rollsFormulaBoundToCharacter() throws Exception {
        var currentUser = authenticatedUser("cleric");
        var characterId = UUID.randomUUID();
        setCurrentUser(currentUser);

        when(rollDiceUseCase.execute(any(User.class), any(CharacterId.class), any(RollDiceCommand.class)))
            .thenReturn(
                new DiceRollResult(
                    characterId,
                    "d20 + [WIS]",
                    "d20 + 2",
                    18,
                    List.of(
                        new DiceTermResult(DiceTermKind.DICE, "d20", "d20", 1, List.of(16), 16),
                        new DiceTermResult(DiceTermKind.CHARACTER_MODIFIER, "[WIS]", "2", 1, List.of(), 2)
                    )
                )
            );

        mockMvc.perform(post("/api/v1/characters/" + characterId + "/rolls")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "formula": "d20 + [WIS]"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.characterId").value(characterId.toString()))
            .andExpect(jsonPath("$.resolvedFormula").value("d20 + 2"));

        var characterIdCaptor = ArgumentCaptor.forClass(CharacterId.class);
        verify(rollDiceUseCase).execute(any(User.class), characterIdCaptor.capture(), any(RollDiceCommand.class));
        org.junit.jupiter.api.Assertions.assertEquals(characterId, characterIdCaptor.getValue().value());
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
