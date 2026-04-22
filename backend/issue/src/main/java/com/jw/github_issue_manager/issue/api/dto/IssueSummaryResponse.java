package com.jw.github_issue_manager.issue.api.dto;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record IssueSummaryResponse(
    PlatformType platform,
    String issueId,
    String numberOrKey,
    String title,
    String state,
    String authorLogin,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastSyncedAt
) {
}
