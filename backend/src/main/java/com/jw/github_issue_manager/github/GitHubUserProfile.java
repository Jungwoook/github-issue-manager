package com.jw.github_issue_manager.github;

public record GitHubUserProfile(
    Long id,
    String login,
    String name,
    String email,
    String avatarUrl
) {
}
