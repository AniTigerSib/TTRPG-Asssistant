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

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.character.CreateCharacterCommand;
import ttrpg.CharManagementService.application.character.CreateCharacterUseCase;
import ttrpg.CharManagementService.application.character.GetCharacterUseCase;
import ttrpg.CharManagementService.application.character.ListAccessibleCharactersUseCase;
import ttrpg.CharManagementService.application.character.UpdateCharacterCommand;
import ttrpg.CharManagementService.application.character.UpdateCharacterUseCase;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.presentation.dto.CharacterResponse;
import ttrpg.CharManagementService.presentation.dto.CreateCharacterRequest;
import ttrpg.CharManagementService.presentation.dto.UpdateCharacterRequest;
import ttrpg.CharManagementService.presentation.mapper.CharacterResponseMapper;

@Validated
@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CreateCharacterUseCase createCharacterUseCase;
    private final GetCharacterUseCase getCharacterUseCase;
    private final ListAccessibleCharactersUseCase listAccessibleCharactersUseCase;
    private final UpdateCharacterUseCase updateCharacterUseCase;
    private final CharacterResponseMapper characterResponseMapper;
    private final ObjectMapper objectMapper;

    @PostMapping
    public CharacterResponse create(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody CreateCharacterRequest request
    ) {
        return characterResponseMapper.toResponse(
            createCharacterUseCase.execute(
                currentUser,
                new CreateCharacterCommand(
                    request.campaignId(),
                    request.gameSystemCode(),
                    request.templateId(),
                    request.name(),
                    request.avatarUrl(),
                    request.status(),
                    objectMapper.valueToTree(request.data())
                )
            )
        );
    }

    @GetMapping("/{characterId}")
    public CharacterResponse getById(
        @AuthenticationPrincipal User currentUser,
        @PathVariable UUID characterId
    ) {
        return characterResponseMapper.toResponse(
            getCharacterUseCase.execute(currentUser, new CharacterId(characterId))
        );
    }

    @GetMapping
    public List<CharacterResponse> listAccessible(@AuthenticationPrincipal User currentUser) {
        return listAccessibleCharactersUseCase.execute(currentUser).stream()
            .map(characterResponseMapper::toResponse)
            .toList();
    }

    @PutMapping("/{characterId}")
    public CharacterResponse update(
        @AuthenticationPrincipal User currentUser,
        @PathVariable UUID characterId,
        @Valid @RequestBody UpdateCharacterRequest request
    ) {
        return characterResponseMapper.toResponse(
            updateCharacterUseCase.execute(
                currentUser,
                new CharacterId(characterId),
                new UpdateCharacterCommand(
                    request.name(),
                    request.avatarUrl(),
                    request.status(),
                    objectMapper.valueToTree(request.data())
                )
            )
        );
    }
}
