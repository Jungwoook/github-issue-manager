package com.jw.github_issue_manager.dto.issue;

import java.time.LocalDateTime;

public record IssueDetailResponse(
    Long githubIssueId,
    Long githubRepositoryId,
    Integer number,
    String title,
    String body,
    String state,
    String authorLogin,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime closedAt,
    LocalDateTime lastSyncedAt
) {
}
