package com.jw.github_issue_manager.connection.api.dto;

public record RegisterValidatedPlatformTokenCommand(
    String accessToken,
    String baseUrl,
    String externalUserId,
    String login,
    String displayName,
    String email,
    String avatarUrl
) {
}
