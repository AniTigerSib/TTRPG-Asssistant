package ttrpg.CharManagementService.application.character;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.gamesystem.GameSystemRulesEngineRegistry;
import ttrpg.CharManagementService.application.shared.TemplateAccessPolicy;
import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.campaign.CampaignMemberRepository;
import ttrpg.CharManagementService.domain.campaign.CampaignRepository;
import ttrpg.CharManagementService.domain.character.Character;
import ttrpg.CharManagementService.domain.character.CharacterRepository;
import ttrpg.CharManagementService.domain.character.CharacterStatus;
import ttrpg.CharManagementService.domain.characterdata.CharacterData;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataRepository;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateRepository;
import ttrpg.CharManagementService.domain.exception.AccessDeniedOperationException;
import ttrpg.CharManagementService.domain.exception.CampaignNotFoundException;
import ttrpg.CharManagementService.domain.exception.CharacterTemplateNotFoundException;
import ttrpg.CharManagementService.domain.exception.GameSystemNotFoundException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemRepository;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;

@Service
@RequiredArgsConstructor
public class CreateCharacterUseCase {

    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository campaignMemberRepository;
    private final CharacterRepository characterRepository;
    private final CharacterDataRepository characterDataRepository;
    private final CharacterTemplateRepository characterTemplateRepository;
    private final GameSystemRepository gameSystemRepository;
    private final GameSystemRulesEngineRegistry rulesEngineRegistry;
    private final TemplateAccessPolicy templateAccessPolicy;

    @Transactional
    public CharacterDetails execute(User currentUser, CreateCharacterCommand command) {
        Checkers.requireNonNull(currentUser, "currentUser");
        Checkers.requireNonNull(command, "command");
        var payload = Checkers.requireNonNull(command.data(), "data");
        var campaignId = command.campaignId() == null ? null : new CampaignId(command.campaignId());

        if (campaignId != null) {
            var campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new CampaignNotFoundException(command.campaignId()));
            if (!campaign.getOwnerId().equals(currentUser.getId())
                && !campaignMemberRepository.existsByCampaignIdAndUserId(campaignId, currentUser.getId())) {
                throw new AccessDeniedOperationException("You must belong to the campaign to create a character in it");
            }
        }

        var templateId = command.templateId() == null ? null : new ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId(command.templateId());
        var template = templateId == null
            ? null
            : characterTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CharacterTemplateNotFoundException(command.templateId()));

        if (template != null && !templateAccessPolicy.canView(template, currentUser)) {
            throw new AccessDeniedOperationException("You do not have access to this template");
        }

        var gameSystem = template != null
            ? gameSystemRepository.findById(template.getGameSystemId())
                .orElseThrow(() -> new GameSystemNotFoundException(template.getGameSystemId().asString()))
            : gameSystemRepository.findByCode(Checkers.requireStringNonBlank(command.gameSystemCode(), "gameSystemCode"))
                .orElseThrow(() -> new GameSystemNotFoundException(command.gameSystemCode()));

        var normalizedData = template == null
            ? payload.deepCopy()
            : rulesEngineRegistry.resolve(gameSystem.getCode()).validateAndNormalize(payload, template.getSchema());

        var character = Character.create(
            currentUser.getId(),
            campaignId,
            gameSystem.getId(),
            templateId,
            command.name(),
            command.avatarUrl(),
            CharacterStatus.fromDatabaseValue(command.status())
        );
        var savedCharacter = characterRepository.save(character);
        var savedData = characterDataRepository.save(CharacterData.create(savedCharacter.getId(), normalizedData));
        return new CharacterDetails(savedCharacter, savedData);
    }
}
