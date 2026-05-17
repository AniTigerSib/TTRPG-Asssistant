package ttrpg.CharManagementService.domain.gamesystem;

import java.util.List;
import java.util.Optional;

public interface GameSystemRepository {

    List<GameSystem> findAll();

    Optional<GameSystem> findById(GameSystemId id);

    Optional<GameSystem> findByCode(String code);
}
