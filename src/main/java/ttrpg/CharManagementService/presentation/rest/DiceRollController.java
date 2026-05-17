package ttrpg.CharManagementService.presentation.rest;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.dice.RollDiceCommand;
import ttrpg.CharManagementService.application.dice.RollDiceUseCase;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.presentation.dto.RollDiceRequest;
import ttrpg.CharManagementService.presentation.dto.RollDiceResponse;
import ttrpg.CharManagementService.presentation.mapper.DiceRollResponseMapper;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DiceRollController {

    private final RollDiceUseCase rollDiceUseCase;
    private final DiceRollResponseMapper mapper;

    @PostMapping("/rolls")
    public RollDiceResponse roll(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody RollDiceRequest request
    ) {
        return mapper.toResponse(rollDiceUseCase.execute(currentUser, new RollDiceCommand(request.formula())));
    }

    @PostMapping("/characters/{characterId}/rolls")
    public RollDiceResponse rollForCharacter(
        @AuthenticationPrincipal User currentUser,
        @PathVariable UUID characterId,
        @Valid @RequestBody RollDiceRequest request
    ) {
        return mapper.toResponse(
            rollDiceUseCase.execute(currentUser, new CharacterId(characterId), new RollDiceCommand(request.formula()))
        );
    }
}
