package ttrpg.CharManagementService.application.shared;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.character.Character;
import ttrpg.CharManagementService.domain.character.CharacterStatus;
import ttrpg.CharManagementService.domain.exception.AccessDeniedOperationException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.user.User;

class CharacterAccessPolicyTest {

    @Test
    void authorAllowedToViewCharacter() {
        var owner = User.create("owner@example.com", "owner", "hashed");
        var character = Character.create(owner.getId(), null, GameSystemId.newId(), null, "Hero", null, CharacterStatus.DRAFT);
        var policy = new CharacterAccessPolicy((campaignId, userId) -> false);

        assertDoesNotThrow(() -> policy.assertCanView(owner, character));
    }

    @Test
    void campaignMemberAllowedToViewCharacter() {
        var owner = User.create("owner@example.com", "owner", "hashed");
        var member = User.create("member@example.com", "member", "hashed");
        var campaignId = new CampaignId(java.util.UUID.randomUUID());
        var character = Character.create(owner.getId(), campaignId, GameSystemId.newId(), null, "Hero", null, CharacterStatus.DRAFT);
        var policy = new CharacterAccessPolicy((checkedCampaignId, checkedUserId) ->
            checkedCampaignId.equals(campaignId) && checkedUserId.equals(member.getId())
        );

        assertDoesNotThrow(() -> policy.assertCanView(member, character));
    }

    @Test
    void outsiderDeniedToViewCharacter() {
        var owner = User.create("owner@example.com", "owner", "hashed");
        var outsider = User.create("outsider@example.com", "outsider", "hashed");
        var campaignId = new CampaignId(java.util.UUID.randomUUID());
        var character = Character.create(owner.getId(), campaignId, GameSystemId.newId(), null, "Hero", null, CharacterStatus.DRAFT);
        var policy = new CharacterAccessPolicy((checkedCampaignId, checkedUserId) -> false);

        assertThrows(AccessDeniedOperationException.class, () -> policy.assertCanView(outsider, character));
    }

    @Test
    void usesActualMembershipRepositoryDataOnEachCheck() {
        var owner = User.create("owner@example.com", "owner", "hashed");
        var user = User.create("member@example.com", "member", "hashed");
        var campaignId = new CampaignId(java.util.UUID.randomUUID());
        var character = Character.create(owner.getId(), campaignId, GameSystemId.newId(), null, "Hero", null, CharacterStatus.DRAFT);
        var membership = new MutableMembershipRepository();
        var policy = new CharacterAccessPolicy(membership);

        membership.allowed = false;
        assertThrows(AccessDeniedOperationException.class, () -> policy.assertCanView(user, character));

        membership.allowed = true;
        assertDoesNotThrow(() -> policy.assertCanView(user, character));
    }

    private static final class MutableMembershipRepository implements ttrpg.CharManagementService.domain.campaign.CampaignMemberRepository {
        private boolean allowed;

        @Override
        public boolean existsByCampaignIdAndUserId(CampaignId campaignId, ttrpg.CharManagementService.domain.user.UserId userId) {
            return allowed;
        }
    }
}
