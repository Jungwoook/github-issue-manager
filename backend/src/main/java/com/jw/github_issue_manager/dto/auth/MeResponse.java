package com.jw.github_issue_manager.dto.auth;

public record MeResponse(
    Long id,
    String displayName,
    String githubLogin,
    String avatarUrl
) {
}
