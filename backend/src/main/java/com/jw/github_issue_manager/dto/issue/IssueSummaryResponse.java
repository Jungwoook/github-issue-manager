package com.jw.github_issue_manager.dto.issue;

import java.time.LocalDateTime;

public record IssueSummaryResponse(
    Long githubIssueId,
    Integer number,
    String title,
    String state,
    String authorLogin,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastSyncedAt
) {
}
