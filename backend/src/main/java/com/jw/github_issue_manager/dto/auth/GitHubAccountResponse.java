package com.jw.github_issue_manager.dto.auth;

import java.time.LocalDateTime;

public record GitHubAccountResponse(
    Long githubUserId,
    String login,
    String avatarUrl,
    String tokenScopes,
    LocalDateTime connectedAt,
    LocalDateTime lastAuthenticatedAt
) {
}
