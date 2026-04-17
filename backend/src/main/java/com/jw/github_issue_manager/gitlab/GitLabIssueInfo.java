package com.jw.github_issue_manager.gitlab;

import java.time.LocalDateTime;

public record GitLabIssueInfo(
    Long id,
    Long projectId,
    Long iid,
    String title,
    String body,
    String state,
    String authorLogin,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime closedAt
) {
}
