package com.jw.github_issue_manager.github;

public record GitHubRepositoryInfo(
    Long id,
    String ownerLogin,
    String name,
    String fullName,
    String description,
    boolean isPrivate,
    String htmlUrl
) {
}
