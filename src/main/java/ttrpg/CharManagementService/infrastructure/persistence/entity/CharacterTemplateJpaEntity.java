package ttrpg.CharManagementService.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

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
@Table(name = "character_templates")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CharacterTemplateJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "game_system_id", nullable = false)
    private UUID gameSystemId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema", nullable = false, columnDefinition = "jsonb")
    private JsonNode schema;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "is_official", nullable = false)
    private boolean official;

    @Column(name = "visibility", nullable = false, length = 255)
    private String visibility;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
