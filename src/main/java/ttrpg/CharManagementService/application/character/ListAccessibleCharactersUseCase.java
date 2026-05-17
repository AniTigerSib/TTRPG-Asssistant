package ttrpg.CharManagementService.application.character;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.character.CharacterRepository;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataRepository;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;

@Service
@RequiredArgsConstructor
public class ListAccessibleCharactersUseCase {

    private final CharacterRepository characterRepository;
    private final CharacterDataRepository characterDataRepository;

    @Transactional(readOnly = true)
    public List<CharacterDetails> execute(User currentUser) {
        Checkers.requireNonNull(currentUser, "currentUser");
        return characterRepository.findAccessibleByUser(currentUser.getId()).stream()
            .map(character -> new CharacterDetails(
                character,
                characterDataRepository.findByCharacterId(character.getId()).orElseThrow()
            ))
            .toList();
    }
}
