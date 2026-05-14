package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public class UnsupportedGameSystemException extends ClientException {

    public UnsupportedGameSystemException(String gameSystemCode) {
        super(
            ErrorCode.GAME_SYSTEM_NOT_SUPPORTED,
            "Game system logic is not available for code: " + gameSystemCode,
            Map.of("gameSystemCode", "Game system logic is not available for code: " + gameSystemCode)
        );
    }
}
