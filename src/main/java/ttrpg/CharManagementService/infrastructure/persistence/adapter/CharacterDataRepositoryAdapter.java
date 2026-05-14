package ttrpg.CharManagementService.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.characterdata.CharacterData;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataRepository;
import ttrpg.CharManagementService.infrastructure.persistence.mapper.CharacterDataPersistenceMapper;
import ttrpg.CharManagementService.infrastructure.persistence.repository.CharacterDataJpaRepository;

@Repository
@RequiredArgsConstructor
public class CharacterDataRepositoryAdapter implements CharacterDataRepository {

    private final CharacterDataJpaRepository jpaRepository;
    private final CharacterDataPersistenceMapper mapper;

    @Override
    public CharacterData save(CharacterData characterData) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(characterData)));
    }

    @Override
    public Optional<CharacterData> findByCharacterId(CharacterId characterId) {
        return jpaRepository.findByCharacterId(characterId.value()).map(mapper::toDomain);
    }
}
