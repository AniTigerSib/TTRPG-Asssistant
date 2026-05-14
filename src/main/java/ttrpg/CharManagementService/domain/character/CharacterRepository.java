package ttrpg.CharManagementService.domain.character;

import java.util.List;
import java.util.Optional;

import ttrpg.CharManagementService.domain.user.UserId;

public interface CharacterRepository {

    Character save(Character character);

    Optional<Character> findById(CharacterId id);

    List<Character> findAccessibleByUser(UserId userId);
}
