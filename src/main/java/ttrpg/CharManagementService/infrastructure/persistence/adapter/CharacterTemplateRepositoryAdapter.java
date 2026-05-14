package ttrpg.CharManagementService.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplate;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateRepository;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.infrastructure.persistence.mapper.CharacterTemplatePersistenceMapper;
import ttrpg.CharManagementService.infrastructure.persistence.repository.CharacterTemplateJpaRepository;

@Repository
@RequiredArgsConstructor
public class CharacterTemplateRepositoryAdapter implements CharacterTemplateRepository {

    private final CharacterTemplateJpaRepository jpaRepository;
    private final CharacterTemplatePersistenceMapper mapper;

    @Override
    public List<CharacterTemplate> findByGameSystemId(GameSystemId gameSystemId) {
        return jpaRepository.findByGameSystemIdOrderByOfficialDescVersionDescNameAsc(gameSystemId.value()).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<CharacterTemplate> findById(CharacterTemplateId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public CharacterTemplate save(CharacterTemplate template) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(template)));
    }
}
