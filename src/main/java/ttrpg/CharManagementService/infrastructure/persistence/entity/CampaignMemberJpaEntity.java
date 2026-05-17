package ttrpg.CharManagementService.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "campaign_members")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignMemberJpaEntity {
    @EmbeddedId
    private CampaignMemberId id;

    @Column(name = "role", nullable = false, length = 255)
    private String role;

    @Embeddable
    @Getter
    @Setter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CampaignMemberId implements Serializable {
        @Column(name = "campaign_id", nullable = false)
        private UUID campaignId;

        @Column(name = "user_id", nullable = false)
        private UUID userId;
    }
}
