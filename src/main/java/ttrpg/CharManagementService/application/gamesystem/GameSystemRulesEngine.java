package ttrpg.CharManagementService.application.gamesystem;

import com.fasterxml.jackson.databind.JsonNode;

public interface GameSystemRulesEngine {

    String getSystemCode();

    JsonNode validateAndNormalize(JsonNode characterData, JsonNode templateSchema);
}
