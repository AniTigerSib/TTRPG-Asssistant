package ttrpg.CharManagementService.application.gamesystem;

import tools.jackson.databind.JsonNode;

public interface GameSystemRulesEngine {

    String getSystemCode();

    JsonNode validateAndNormalize(JsonNode characterData, JsonNode templateSchema);
}
