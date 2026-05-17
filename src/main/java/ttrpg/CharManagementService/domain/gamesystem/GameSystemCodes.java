package ttrpg.CharManagementService.domain.gamesystem;

import java.util.Locale;

import ttrpg.CharManagementService.domain.shared.Checkers;

public final class GameSystemCodes {
    public static final String DND5E = "DND5E";
    public static final String FATE_CORE = "FATE_CORE";

    private GameSystemCodes() {}

    public static String normalize(String code) {
        return Checkers.requireStringNonBlank(code, "gameSystemCode").trim().toUpperCase(Locale.ROOT);
    }
}
