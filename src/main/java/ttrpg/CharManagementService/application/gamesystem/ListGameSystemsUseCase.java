package ttrpg.CharManagementService.application.gamesystem;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.gamesystem.GameSystem;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemRepository;

@Service
@RequiredArgsConstructor
public class ListGameSystemsUseCase {

    private final GameSystemRepository gameSystemRepository;

    @Transactional(readOnly = true)
    public List<GameSystem> execute() {
        return List.copyOf(gameSystemRepository.findAll());
    }
}
