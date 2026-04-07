package com.jw.github_issue_manager.dto.auth;

import java.time.LocalDateTime;

public record GitHubTokenStatusResponse(
    boolean connected,
    String githubLogin,
    String tokenScopes,
    LocalDateTime tokenVerifiedAt
) {
}
