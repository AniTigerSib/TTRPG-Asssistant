package ttrpg.CharManagementService.application.dice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ttrpg.CharManagementService.application.shared.CharacterAccessPolicy;
import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.campaign.CampaignMemberRepository;
import ttrpg.CharManagementService.domain.character.Character;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.character.CharacterRepository;
import ttrpg.CharManagementService.domain.character.CharacterSnapshot;
import ttrpg.CharManagementService.domain.character.CharacterStatus;
import ttrpg.CharManagementService.domain.characterdata.CharacterData;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataRepository;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataSnapshot;
import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystem;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemCodes;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemRepository;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemSnapshot;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;

class RollDiceUseCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void rollsFateDiceFormulaWithoutCharacter() {
        var currentUser = User.create("test@example.com", "roller", "hashed");
        var useCase = createUseCase(List.of(-1, 1, 0, 1));

        var result = useCase.execute(currentUser, new RollDiceCommand("4dF + 2"));

        assertEquals("4dF + 2", result.resolvedFormula());
        assertEquals(3, result.total());
        assertEquals(List.of(-1, 1, 0, 1), result.terms().getFirst().rolls());
        assertEquals(2, result.terms().get(1).value());
    }

    @Test
    void resolvesDndCharacterModifiersFromBoundCharacter() throws Exception {
        var currentUser = User.create("test@example.com", "cleric", "hashed");
        var characterId = new CharacterId(UUID.randomUUID());
        var dndSystem = dndSystem();
        var character = Character.restore(
            new CharacterSnapshot(
                characterId,
                currentUser.getId(),
                null,
                dndSystem.getId(),
                null,
                "Sister Arin",
                null,
                CharacterStatus.ACTIVE,
                Instant.now(),
                Instant.now()
            )
        );
        var characterData = CharacterData.restore(
            new CharacterDataSnapshot(
                ttrpg.CharManagementService.domain.characterdata.CharacterDataId.newId(),
                characterId,
                objectMapper.readTree("""
                    {
                      "proficiencyBonus": 3,
                      "abilities": {
                        "strength": 10,
                        "dexterity": 12,
                        "constitution": 14,
                        "intelligence": 11,
                        "wisdom": 15,
                        "charisma": 13
                      }
                    }
                    """),
                1,
                Instant.now()
            )
        );

        var useCase = new RollDiceUseCase(
            new InMemoryCharacterRepository(Map.of(characterId, character)),
            new InMemoryCharacterDataRepository(Map.of(characterId, characterData)),
            new InMemoryGameSystemRepository(Map.of(dndSystem.getId(), dndSystem)),
            new CharacterAccessPolicy((campaignId, userId) -> false),
            new DiceFormulaEvaluator(new FixedDiceRandomSource(List.of(14)))
        );

        var result = useCase.execute(currentUser, characterId, new RollDiceCommand("d20 + [PROF] + [WIS]"));

        assertEquals("d20 + 3 + 2", result.resolvedFormula());
        assertEquals(19, result.total());
        assertEquals(14, result.terms().getFirst().value());
        assertEquals(3, result.terms().get(1).value());
        assertEquals(2, result.terms().get(2).value());
    }

    @Test
    void rejectsCharacterModifierWithoutBoundCharacter() {
        var currentUser = User.create("test@example.com", "roller", "hashed");
        var useCase = createUseCase(List.of(12));

        var exception = assertThrows(
            InvalidInputException.class,
            () -> useCase.execute(currentUser, new RollDiceCommand("d20 + [WIS]"))
        );

        assertEquals("Character modifier [WIS] requires a bound character", exception.getPublicMessage());
    }

    private RollDiceUseCase createUseCase(List<Integer> rolls) {
        return new RollDiceUseCase(
            new InMemoryCharacterRepository(Map.of()),
            new InMemoryCharacterDataRepository(Map.of()),
            new InMemoryGameSystemRepository(Map.of()),
            new CharacterAccessPolicy((campaignId, userId) -> false),
            new DiceFormulaEvaluator(new FixedDiceRandomSource(rolls))
        );
    }

    private GameSystem dndSystem() {
        return GameSystem.restore(
            new GameSystemSnapshot(
                new GameSystemId(UUID.fromString("7d4298e7-f415-49f8-a9dd-c5677d00ce90")),
                GameSystemCodes.DND5E,
                "Dungeons & Dragons 5e",
                "2014",
                null,
                Instant.now()
            )
        );
    }

    private static final class FixedDiceRandomSource implements DiceRandomSource {
        private final Queue<Integer> values;

        private FixedDiceRandomSource(List<Integer> values) {
            this.values = new ArrayDeque<>(values);
        }

        @Override
        public int nextInt(int minInclusive, int maxInclusive) {
            if (values.isEmpty()) {
                throw new AssertionError("No more deterministic dice values configured");
            }
            var value = values.remove();
            if (value < minInclusive || value > maxInclusive) {
                throw new AssertionError("Deterministic dice value out of requested bounds");
            }
            return value;
        }
    }

    private static final class InMemoryCharacterRepository implements CharacterRepository {
        private final Map<CharacterId, Character> storage;

        private InMemoryCharacterRepository(Map<CharacterId, Character> storage) {
            this.storage = new HashMap<>(storage);
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
            return storage.values().stream().filter(character -> character.getOwnerId().equals(userId)).toList();
        }
    }

    private static final class InMemoryCharacterDataRepository implements CharacterDataRepository {
        private final Map<CharacterId, CharacterData> storage;

        private InMemoryCharacterDataRepository(Map<CharacterId, CharacterData> storage) {
            this.storage = new HashMap<>(storage);
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

    private static final class InMemoryGameSystemRepository implements GameSystemRepository {
        private final Map<GameSystemId, GameSystem> storage;

        private InMemoryGameSystemRepository(Map<GameSystemId, GameSystem> storage) {
            this.storage = new HashMap<>(storage);
        }

        @Override
        public List<GameSystem> findAll() {
            return List.copyOf(storage.values());
        }

        @Override
        public Optional<GameSystem> findById(GameSystemId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<GameSystem> findByCode(String code) {
            return storage.values().stream().filter(item -> item.getCode().equalsIgnoreCase(code)).findFirst();
        }
    }
}
