package ttrpg.CharManagementService.application.character;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.shared.CharacterAccessPolicy;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.character.CharacterRepository;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataRepository;
import ttrpg.CharManagementService.domain.exception.CharacterNotFoundException;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;

@Service
@RequiredArgsConstructor
public class GetCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final CharacterDataRepository characterDataRepository;
    private final CharacterAccessPolicy characterAccessPolicy;

    @Transactional(readOnly = true)
    public CharacterDetails execute(User currentUser, CharacterId characterId) {
        Checkers.requireNonNull(currentUser, "currentUser");
        Checkers.requireNonNull(characterId, "characterId");
        var character = characterRepository.findById(characterId)
            .orElseThrow(() -> new CharacterNotFoundException(characterId.value()));
        characterAccessPolicy.assertCanView(currentUser, character);
        var characterData = characterDataRepository.findByCharacterId(characterId)
            .orElseThrow(() -> new CharacterNotFoundException(characterId.value()));
        return new CharacterDetails(character, characterData);
    }
}
