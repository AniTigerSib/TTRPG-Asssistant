package ttrpg.CharManagementService.presentation.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.gamesystem.CreateCharacterTemplateUseCase;
import ttrpg.CharManagementService.application.gamesystem.ListCharacterTemplatesUseCase;
import ttrpg.CharManagementService.application.gamesystem.ListGameSystemsUseCase;
import ttrpg.CharManagementService.application.gamesystem.UpdateCharacterTemplateUseCase;
import ttrpg.CharManagementService.application.gamesystem.UpsertCharacterTemplateCommand;
import ttrpg.CharManagementService.application.gamesystem.ValidateCharacterTemplateCommand;
import ttrpg.CharManagementService.application.gamesystem.ValidateCharacterTemplateUseCase;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.presentation.dto.CharacterTemplateResponse;
import ttrpg.CharManagementService.presentation.dto.GameSystemResponse;
import ttrpg.CharManagementService.presentation.dto.UpsertCharacterTemplateRequest;
import ttrpg.CharManagementService.presentation.dto.ValidateCharacterRequest;
import ttrpg.CharManagementService.presentation.dto.ValidateCharacterResponse;
import ttrpg.CharManagementService.presentation.mapper.GameCatalogResponseMapper;
import tools.jackson.databind.ObjectMapper;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GameCatalogController {

    private final ListGameSystemsUseCase listGameSystemsUseCase;
    private final ListCharacterTemplatesUseCase listCharacterTemplatesUseCase;
    private final ValidateCharacterTemplateUseCase validateCharacterTemplateUseCase;
    private final CreateCharacterTemplateUseCase createCharacterTemplateUseCase;
    private final UpdateCharacterTemplateUseCase updateCharacterTemplateUseCase;
    private final GameCatalogResponseMapper mapper;
    private final ObjectMapper objectMapper;

    @GetMapping("/game-systems")
    public List<GameSystemResponse> listGameSystems() {
        return listGameSystemsUseCase.execute().stream()
            .map(mapper::toGameSystemResponse)
            .toList();
    }

    @GetMapping("/game-systems/{gameSystemCode}/templates")
    public List<CharacterTemplateResponse> listCharacterTemplates(
        @PathVariable String gameSystemCode,
        @AuthenticationPrincipal User currentUser
    ) {
        return listCharacterTemplatesUseCase.execute(gameSystemCode, currentUser).stream()
            .map(mapper::toCharacterTemplateResponse)
            .toList();
    }

    @PostMapping("/character-templates/{templateId}/validate")
    public ValidateCharacterResponse validateCharacter(
        @PathVariable UUID templateId,
        @Valid @RequestBody ValidateCharacterRequest request
    ) {
        return mapper.toValidateCharacterResponse(
            validateCharacterTemplateUseCase.execute(
                new ValidateCharacterTemplateCommand(templateId, objectMapper.valueToTree(request.characterData()))
            )
        );
    }

    @PostMapping("/character-templates")
    public CharacterTemplateResponse createCharacterTemplate(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody UpsertCharacterTemplateRequest request
    ) {
        return mapper.toCharacterTemplateResponse(
            createCharacterTemplateUseCase.execute(currentUser, toUpsertCommand(request))
        );
    }

    @PutMapping("/character-templates/{templateId}")
    public CharacterTemplateResponse updateCharacterTemplate(
        @AuthenticationPrincipal User currentUser,
        @PathVariable UUID templateId,
        @Valid @RequestBody UpsertCharacterTemplateRequest request
    ) {
        return mapper.toCharacterTemplateResponse(
            updateCharacterTemplateUseCase.execute(currentUser, new CharacterTemplateId(templateId), toUpsertCommand(request))
        );
    }

    private UpsertCharacterTemplateCommand toUpsertCommand(UpsertCharacterTemplateRequest request) {
        return new UpsertCharacterTemplateCommand(
            request.gameSystemId(),
            request.name(),
            objectMapper.valueToTree(request.schema()),
            request.version(),
            request.official(),
            request.visibility()
        );
    }
}
