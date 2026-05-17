package ttrpg.CharManagementService.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.character.Character;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.character.CharacterRepository;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.infrastructure.persistence.mapper.CharacterPersistenceMapper;
import ttrpg.CharManagementService.infrastructure.persistence.repository.CharacterJpaRepository;

@Repository
@RequiredArgsConstructor
public class CharacterRepositoryAdapter implements CharacterRepository {

    private final CharacterJpaRepository jpaRepository;
    private final CharacterPersistenceMapper mapper;

    @Override
    public Character save(Character character) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(character)));
    }

    @Override
    public Optional<Character> findById(CharacterId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Character> findAccessibleByUser(UserId userId) {
        return jpaRepository.findAccessibleByUserId(userId.value()).stream().map(mapper::toDomain).toList();
    }
}
