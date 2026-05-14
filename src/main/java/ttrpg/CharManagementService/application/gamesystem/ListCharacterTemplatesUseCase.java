package ttrpg.CharManagementService.application.gamesystem;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplate;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateRepository;
import ttrpg.CharManagementService.domain.exception.GameSystemNotFoundException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemRepository;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.application.shared.TemplateAccessPolicy;

@Service
@RequiredArgsConstructor
public class ListCharacterTemplatesUseCase {

    private final GameSystemRepository gameSystemRepository;
    private final CharacterTemplateRepository characterTemplateRepository;
    private final TemplateAccessPolicy templateAccessPolicy;

    @Transactional(readOnly = true)
    public List<CharacterTemplate> execute(String gameSystemCode, User currentUser) {
        var normalizedCode = Checkers.requireStringNonBlank(gameSystemCode, "gameSystemCode");
        var gameSystem = gameSystemRepository.findByCode(normalizedCode)
            .orElseThrow(() -> new GameSystemNotFoundException(normalizedCode));
        return characterTemplateRepository.findByGameSystemId(gameSystem.getId()).stream()
            .filter(template -> templateAccessPolicy.canView(template, currentUser))
            .toList();
    }
}
