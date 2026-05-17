package ttrpg.CharManagementService.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "characters")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CharacterJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "game_system_id", nullable = false)
    private UUID gameSystemId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "status", nullable = false, length = 255)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
