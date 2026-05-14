package ttrpg.CharManagementService.application.gamesystem;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.exception.InvariantViolationException;
import ttrpg.CharManagementService.domain.exception.UnsupportedGameSystemException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemCodes;

@Component
public class GameSystemRulesEngineRegistry {

    private final Map<String, GameSystemRulesEngine> enginesByCode;

    public GameSystemRulesEngineRegistry(List<GameSystemRulesEngine> engines) {
        this.enginesByCode = engines.stream()
            .collect(Collectors.toUnmodifiableMap(
                engine -> GameSystemCodes.normalize(engine.getSystemCode()),
                Function.identity(),
                (left, right) -> {
                    throw new InvariantViolationException(
                        "Duplicate game system rules engine for code: "
                            + left.getSystemCode().toUpperCase(Locale.ROOT)
                    );
                }
            ));
    }

    public GameSystemRulesEngine resolve(String gameSystemCode) {
        var normalizedCode = GameSystemCodes.normalize(gameSystemCode);
        var engine = enginesByCode.get(normalizedCode);
        if (engine == null) {
            throw new UnsupportedGameSystemException(normalizedCode);
        }
        return engine;
    }
}
