package com.jw.github_issue_manager.gitlab;

public record GitLabUserProfile(
    Long id,
    String username,
    String name,
    String email,
    String avatarUrl
) {
}
