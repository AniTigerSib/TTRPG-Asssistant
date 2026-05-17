package ttrpg.CharManagementService.domain.exception;

public class GameSystemNotFoundException extends ResourceNotFoundException {

    public GameSystemNotFoundException(String gameSystemCode) {
        super(ErrorCode.GAME_SYSTEM_NOT_FOUND, "Game system not found: " + gameSystemCode);
    }
}
