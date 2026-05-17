package ttrpg.CharManagementService.application.auth;

import java.net.InetAddress;

public record RefreshAuthenticationCommand(
    String refreshToken,
    String userAgent,
    InetAddress ipAddress
) {}
