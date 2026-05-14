package ttrpg.CharManagementService.application.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.application.gamesystem.GameSystemRulesEngine;
import ttrpg.CharManagementService.application.gamesystem.GameSystemRulesEngineRegistry;
import ttrpg.CharManagementService.application.shared.CharacterAccessPolicy;
import ttrpg.CharManagementService.application.shared.TemplateAccessPolicy;
import ttrpg.CharManagementService.domain.campaign.Campaign;
import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.campaign.CampaignRepository;
import ttrpg.CharManagementService.domain.character.Character;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.character.CharacterRepository;
import ttrpg.CharManagementService.domain.character.CharacterSnapshot;
import ttrpg.CharManagementService.domain.character.CharacterStatus;
import ttrpg.CharManagementService.domain.characterdata.CharacterData;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataRepository;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataSnapshot;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplate;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateRepository;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateSnapshot;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateVisibility;
import ttrpg.CharManagementService.domain.exception.AccessDeniedOperationException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystem;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemCodes;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemRepository;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemSnapshot;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class CreateCharacterUseCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createsCharacterWithoutTemplateAndSkipsValidation() throws Exception {
        var gameSystem = fateCoreSystem();
        var useCase = createUseCase(gameSystem, null);
        var currentUser = User.create("test@example.com", "hero", "hashed");
        var data = objectMapper.readTree("""
            {
              "freeform": true
            }
            """);

        var result = useCase.execute(
            currentUser,
            new CreateCharacterCommand(null, gameSystem.getCode(), null, "Free Hero", null, "draft", data)
        );

        assertEquals("Free Hero", result.character().getName());
        assertEquals(CharacterStatus.DRAFT, result.character().getStatus());
        assertEquals(true, result.characterData().getData().get("freeform").asBoolean());
    }

    @Test
    void rejectsCharacterCreationInForeignCampaign() throws Exception {
        var gameSystem = fateCoreSystem();
        var currentUser = User.create("test@example.com", "hero", "hashed");
        var foreignCampaign = Campaign.restore(
            new ttrpg.CharManagementService.domain.campaign.CampaignSnapshot(
                new CampaignId(UUID.randomUUID()),
                UserId.newId(),
                gameSystem.getId(),
                "Foreign campaign",
                null,
                ttrpg.CharManagementService.domain.campaign.CampaignVisibility.PRIVATE,
                Instant.now(),
                Instant.now()
            )
        );
        var useCase = new CreateCharacterUseCase(
            new InMemoryCampaignRepository(Map.of(foreignCampaign.getId(), foreignCampaign)),
            (campaignId, userId) -> false,
            new InMemoryCharacterRepository(),
            new InMemoryCharacterDataRepository(),
            new InMemoryCharacterTemplateRepository(),
            new InMemoryGameSystemRepository(Map.of(gameSystem.getId(), gameSystem)),
            new GameSystemRulesEngineRegistry(List.of(new PassThroughEngine())),
            new TemplateAccessPolicy()
        );

        var exception = assertThrows(
            AccessDeniedOperationException.class,
            () -> useCase.execute(
                currentUser,
                new CreateCharacterCommand(
                    foreignCampaign.getId().value(),
                    gameSystem.getCode(),
                    null,
                    "Intruder",
                    null,
                    "draft",
                    objectMapper.createObjectNode()
                )
            )
        );

        assertEquals("You must belong to the campaign to create a character in it", exception.getPublicMessage());
    }

    private CreateCharacterUseCase createUseCase(GameSystem gameSystem, CharacterTemplate template) {
        var templates = new InMemoryCharacterTemplateRepository();
        if (template != null) {
            templates.save(template);
        }
        return new CreateCharacterUseCase(
            new InMemoryCampaignRepository(Map.of()),
            (campaignId, userId) -> false,
            new InMemoryCharacterRepository(),
            new InMemoryCharacterDataRepository(),
            templates,
            new InMemoryGameSystemRepository(Map.of(gameSystem.getId(), gameSystem)),
            new GameSystemRulesEngineRegistry(List.of(new PassThroughEngine())),
            new TemplateAccessPolicy()
        );
    }

    private GameSystem fateCoreSystem() {
        return GameSystem.restore(
            new GameSystemSnapshot(
                new GameSystemId(UUID.randomUUID()),
                GameSystemCodes.FATE_CORE,
                "Fate Core",
                "4th Edition",
                null,
                Instant.now()
            )
        );
    }

    private static final class PassThroughEngine implements GameSystemRulesEngine {
        @Override
        public String getSystemCode() {
            return GameSystemCodes.FATE_CORE;
        }

        @Override
        public tools.jackson.databind.JsonNode validateAndNormalize(
            tools.jackson.databind.JsonNode characterData,
            tools.jackson.databind.JsonNode templateSchema
        ) {
            return characterData.deepCopy();
        }
    }

    private static final class InMemoryCampaignRepository implements CampaignRepository {
        private final Map<CampaignId, Campaign> storage;

        private InMemoryCampaignRepository(Map<CampaignId, Campaign> storage) {
            this.storage = storage;
        }

        @Override
        public Optional<Campaign> findById(CampaignId id) {
            return Optional.ofNullable(storage.get(id));
        }
    }

    private static final class InMemoryGameSystemRepository implements GameSystemRepository {
        private final Map<GameSystemId, GameSystem> storage;

        private InMemoryGameSystemRepository(Map<GameSystemId, GameSystem> storage) {
            this.storage = storage;
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

    private static final class InMemoryCharacterTemplateRepository implements CharacterTemplateRepository {
        private final Map<CharacterTemplateId, CharacterTemplate> storage = new HashMap<>();

        @Override
        public List<CharacterTemplate> findByGameSystemId(GameSystemId gameSystemId) {
            return storage.values().stream().filter(item -> item.getGameSystemId().equals(gameSystemId)).toList();
        }

        @Override
        public Optional<CharacterTemplate> findById(CharacterTemplateId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public CharacterTemplate save(CharacterTemplate template) {
            storage.put(template.getId(), template);
            return template;
        }
    }

    private static final class InMemoryCharacterRepository implements CharacterRepository {
        private final Map<CharacterId, Character> storage = new HashMap<>();

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
