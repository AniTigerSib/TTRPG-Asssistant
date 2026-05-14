package ttrpg.CharManagementService.presentation.mapper;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.character.CharacterDetails;
import ttrpg.CharManagementService.presentation.dto.CharacterResponse;

@Component
@RequiredArgsConstructor
public class CharacterResponseMapper {

    private final ObjectMapper objectMapper;

    public CharacterResponse toResponse(CharacterDetails details) {
        var character = details.character();
        var data = details.characterData();
        return new CharacterResponse(
            character.getId().value(),
            character.getOwnerId().value(),
            character.getCampaignId() == null ? null : character.getCampaignId().value(),
            character.getGameSystemId().value(),
            character.getTemplateId() == null ? null : character.getTemplateId().value(),
            character.getName(),
            character.getAvatarUrl(),
            character.getStatus().toDatabaseValue(),
            objectMapper.convertValue(data.getData(), Object.class),
            data.getVersion(),
            character.getCreatedAt(),
            character.getUpdatedAt(),
            data.getUpdatedAt()
        );
    }
}
