package ttrpg.CharManagementService.application.shared;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.campaign.CampaignMemberRepository;
import ttrpg.CharManagementService.domain.character.Character;
import ttrpg.CharManagementService.domain.exception.AccessDeniedOperationException;
import ttrpg.CharManagementService.domain.user.User;

@Component
@RequiredArgsConstructor
public class CharacterAccessPolicy {

    private final CampaignMemberRepository campaignMemberRepository;

    public void assertCanView(User currentUser, Character character) {
        if (!canView(currentUser, character)) {
            throw new AccessDeniedOperationException("You do not have access to this character");
        }
    }

    public void assertCanEdit(User currentUser, Character character) {
        if (!character.getOwnerId().equals(currentUser.getId())) {
            throw new AccessDeniedOperationException("Only the character author can edit this character");
        }
    }

    public boolean canView(User currentUser, Character character) {
        if (character.getOwnerId().equals(currentUser.getId())) {
            return true;
        }
        if (character.getCampaignId() == null) {
            return false;
        }
        return campaignMemberRepository.existsByCampaignIdAndUserId(character.getCampaignId(), currentUser.getId());
    }
}
