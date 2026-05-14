package ttrpg.CharManagementService.application.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import ttrpg.CharManagementService.application.shared.CharacterAccessPolicy;
import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.campaign.CampaignMemberRepository;
import ttrpg.CharManagementService.domain.character.Character;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.character.CharacterRepository;
import ttrpg.CharManagementService.domain.character.CharacterStatus;
import ttrpg.CharManagementService.domain.characterdata.CharacterData;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataRepository;
import ttrpg.CharManagementService.domain.exception.AccessDeniedOperationException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;

class GetCharacterUseCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void returnsCharacterToOwner() throws Exception {
        var owner = User.create("test@example.com", "owner", "hashed");
        var character = Character.create(owner.getId(), null, GameSystemId.newId(), null, "Hero", null, CharacterStatus.DRAFT);
        var data = CharacterData.create(character.getId(), objectMapper.readTree("{\"hp\":10}"));

        var useCase = new GetCharacterUseCase(
            new InMemoryCharacterRepository(character),
            new InMemoryCharacterDataRepository(data),
            new CharacterAccessPolicy((campaignId, userId) -> false)
        );

        var result = useCase.execute(owner, character.getId());

        assertEquals("Hero", result.character().getName());
        assertEquals(10, result.characterData().getData().get("hp").asInt());
    }

    @Test
    void rejectsUnrelatedViewer() throws Exception {
        var owner = User.create("test@example.com", "owner", "hashed");
        var stranger = User.create("other@example.com", "stranger", "hashed");
        var character = Character.create(owner.getId(), null, GameSystemId.newId(), null, "Hero", null, CharacterStatus.DRAFT);
        var data = CharacterData.create(character.getId(), objectMapper.readTree("{\"hp\":10}"));

        var useCase = new GetCharacterUseCase(
            new InMemoryCharacterRepository(character),
            new InMemoryCharacterDataRepository(data),
            new CharacterAccessPolicy((campaignId, userId) -> false)
        );

        var exception = assertThrows(
            AccessDeniedOperationException.class,
            () -> useCase.execute(stranger, character.getId())
        );

        assertEquals("You do not have access to this character", exception.getPublicMessage());
    }

    private static final class InMemoryCharacterRepository implements CharacterRepository {
        private final Map<CharacterId, Character> storage = new HashMap<>();

        private InMemoryCharacterRepository(Character character) {
            storage.put(character.getId(), character);
        }

        @Override
        public Character save(Character character) {
            storage.put(character.getId(), character);
            return character;
        }

        @Override
        public Optional<Character> findById(CharacterId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Character> findAccessibleByUser(UserId userId) {
            return storage.values().stream().filter(item -> item.getOwnerId().equals(userId)).toList();
        }
    }

    private static final class InMemoryCharacterDataRepository implements CharacterDataRepository {
        private final Map<CharacterId, CharacterData> storage = new HashMap<>();

        private InMemoryCharacterDataRepository(CharacterData characterData) {
            storage.put(characterData.getCharacterId(), characterData);
        }

        @Override
        public CharacterData save(CharacterData characterData) {
            storage.put(characterData.getCharacterId(), characterData);
            return characterData;
        }

        @Override
        public Optional<CharacterData> findByCharacterId(CharacterId characterId) {
            return Optional.ofNullable(storage.get(characterId));
        }
    }
}
