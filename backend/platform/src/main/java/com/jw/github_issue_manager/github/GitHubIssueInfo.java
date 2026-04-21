package com.jw.github_issue_manager.github;

import java.time.LocalDateTime;

public record GitHubIssueInfo(
    Long id,
    Integer number,
    String title,
    String body,
    String state,
    String authorLogin,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime closedAt
) {
}
