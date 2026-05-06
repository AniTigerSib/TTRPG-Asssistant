package ttrpg.CharManagementService.presentation.mapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.auth.AuthenticationResult;
import ttrpg.CharManagementService.presentation.dto.AuthenticationResponse;
import ttrpg.CharManagementService.presentation.dto.TokenResponse;

@Component
@RequiredArgsConstructor
public class AuthenticationResponseMapper {

    private final UserResponseMapper userResponseMapper;

    public AuthenticationResponse toResponse(AuthenticationResult result) {
        return new AuthenticationResponse(
            userResponseMapper.toUserResponse(result.user()),
            new TokenResponse(result.tokens().accessToken(), result.tokens().accessTokenExpiresAt()),
            new TokenResponse(result.tokens().refreshToken(), result.tokens().refreshTokenExpiresAt())
        );
    }
}
