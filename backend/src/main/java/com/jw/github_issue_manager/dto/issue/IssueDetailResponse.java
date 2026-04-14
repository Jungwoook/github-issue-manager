package com.jw.github_issue_manager.dto.issue;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record IssueDetailResponse(
    PlatformType platform,
    String issueId,
    String repositoryId,
    String numberOrKey,
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
