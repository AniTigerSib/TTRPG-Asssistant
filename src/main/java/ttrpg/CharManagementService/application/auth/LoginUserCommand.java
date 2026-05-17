package ttrpg.CharManagementService.application.auth;

import java.net.InetAddress;

public record LoginUserCommand(
    String login,
    String password,
    String userAgent,
    InetAddress ipAddress
) {}
