package ttrpg.CharManagementService.presentation.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ttrpg.CharManagementService.application.gamesystem.CharacterTemplateValidationResult;
import ttrpg.CharManagementService.application.gamesystem.CreateCharacterTemplateUseCase;
import ttrpg.CharManagementService.application.gamesystem.ListCharacterTemplatesUseCase;
import ttrpg.CharManagementService.application.gamesystem.ListGameSystemsUseCase;
import ttrpg.CharManagementService.application.gamesystem.UpdateCharacterTemplateUseCase;
import ttrpg.CharManagementService.application.gamesystem.ValidateCharacterTemplateCommand;
import ttrpg.CharManagementService.application.gamesystem.ValidateCharacterTemplateUseCase;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplate;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateSnapshot;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateVisibility;
import ttrpg.CharManagementService.domain.exception.CharacterTemplateNotFoundException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystem;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemSnapshot;
import ttrpg.CharManagementService.presentation.errors.GlobalExceptionHandler;
import ttrpg.CharManagementService.presentation.mapper.GameCatalogResponseMapper;

class GameCatalogControllerTest {

    private final ListGameSystemsUseCase listGameSystemsUseCase = Mockito.mock(ListGameSystemsUseCase.class);
    private final ListCharacterTemplatesUseCase listCharacterTemplatesUseCase = Mockito.mock(ListCharacterTemplatesUseCase.class);
    private final ValidateCharacterTemplateUseCase validateCharacterTemplateUseCase = Mockito.mock(
        ValidateCharacterTemplateUseCase.class
    );
    private final CreateCharacterTemplateUseCase createCharacterTemplateUseCase = Mockito.mock(CreateCharacterTemplateUseCase.class);
    private final UpdateCharacterTemplateUseCase updateCharacterTemplateUseCase = Mockito.mock(UpdateCharacterTemplateUseCase.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new GameCatalogController(
                    listGameSystemsUseCase,
                    listCharacterTemplatesUseCase,
                    validateCharacterTemplateUseCase,
                    createCharacterTemplateUseCase,
                    updateCharacterTemplateUseCase,
                    new GameCatalogResponseMapper(objectMapper),
                    objectMapper
                )
            )
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void listsGameSystems() throws Exception {
        when(listGameSystemsUseCase.execute()).thenReturn(List.of(fateCoreSystem()));

        mockMvc.perform(get("/api/v1/game-systems"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("FATE_CORE"))
            .andExpect(jsonPath("$[0].name").value("Fate Core"))
            .andExpect(jsonPath("$[0].version").value("4th Edition"));
    }

    @Test
    void listsCharacterTemplatesForGameSystem() throws Exception {
        when(listCharacterTemplatesUseCase.execute("FATE_CORE", null)).thenReturn(List.of(fateCoreTemplate()));

        mockMvc.perform(get("/api/v1/game-systems/FATE_CORE/templates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Official Fate Core Character Sheet"))
            .andExpect(jsonPath("$[0].official").value(true))
            .andExpect(jsonPath("$[0].visibility").value("visible"))
            .andExpect(jsonPath("$[0].schema.type").value("fate-core.character-sheet"));
    }

    @Test
    void validatesCharacterByTemplate() throws Exception {
        var template = fateCoreTemplate();
        when(validateCharacterTemplateUseCase.execute(any(ValidateCharacterTemplateCommand.class)))
            .thenReturn(
                new CharacterTemplateValidationResult(
                    fateCoreSystem(),
                    template,
                    objectMapper.readTree("""
                        {
                          "name": "Tara Vale",
                          "refresh": 3
                        }
                        """)
                )
            );

        mockMvc.perform(post("/api/v1/character-templates/" + template.getId().value() + "/validate")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "characterData": {
                        "name": "Tara Vale"
                      }
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.gameSystemCode").value("FATE_CORE"))
            .andExpect(jsonPath("$.templateName").value("Official Fate Core Character Sheet"))
            .andExpect(jsonPath("$.normalizedCharacterData.refresh").value(3));
    }

    @Test
    void mapsMissingTemplateToNotFound() throws Exception {
        var missingTemplateId = UUID.fromString("0f821a14-3b3b-4ffb-a6be-48f3db41917f");
        when(validateCharacterTemplateUseCase.execute(any(ValidateCharacterTemplateCommand.class)))
            .thenThrow(new CharacterTemplateNotFoundException(missingTemplateId));

        mockMvc.perform(post("/api/v1/character-templates/" + missingTemplateId + "/validate")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "characterData": {
                        "name": "Tara Vale"
                      }
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("CHARACTER_TEMPLATE_NOT_FOUND"));
    }

    private GameSystem fateCoreSystem() {
        return GameSystem.restore(
            new GameSystemSnapshot(
                new GameSystemId(UUID.fromString("690b1746-fca0-4288-a21f-80855b0d46c2")),
                "FATE_CORE",
                "Fate Core",
                "4th Edition",
                "Official Fate Core character creation and validation rules.",
                Instant.parse("2026-05-14T08:00:00Z")
            )
        );
    }

    private CharacterTemplate fateCoreTemplate() throws Exception {
        return CharacterTemplate.restore(
            new CharacterTemplateSnapshot(
                new CharacterTemplateId(UUID.fromString("fdb6a7c1-aa64-48bf-8729-3873fb495516")),
                new GameSystemId(UUID.fromString("690b1746-fca0-4288-a21f-80855b0d46c2")),
                "Official Fate Core Character Sheet",
                objectMapper.readTree("""
                    {
                      "type": "fate-core.character-sheet"
                    }
                    """),
                1,
                true,
                CharacterTemplateVisibility.VISIBLE,
                Instant.parse("2026-05-14T08:05:00Z")
            )
        );
    }
}
