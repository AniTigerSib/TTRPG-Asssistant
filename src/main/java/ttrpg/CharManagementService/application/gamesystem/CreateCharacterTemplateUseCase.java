package ttrpg.CharManagementService.application.gamesystem;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.shared.TemplateAccessPolicy;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplate;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateVisibility;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateRepository;
import ttrpg.CharManagementService.domain.user.User;

@Service
@RequiredArgsConstructor
public class CreateCharacterTemplateUseCase {

    private final CharacterTemplateRepository characterTemplateRepository;
    private final TemplateAccessPolicy templateAccessPolicy;

    @Transactional
    public CharacterTemplate execute(User currentUser, UpsertCharacterTemplateCommand command) {
        templateAccessPolicy.assertCanManage(currentUser);
        Checkers.requireNonNull(command, "command");
        var template = CharacterTemplate.create(
            new GameSystemId(command.gameSystemId()),
            command.name(),
            Checkers.requireNonNull(command.schema(), "schema"),
            command.version(),
            command.official(),
            CharacterTemplateVisibility.fromDatabaseValue(command.visibility())
        );
        return characterTemplateRepository.save(template);
    }
}
