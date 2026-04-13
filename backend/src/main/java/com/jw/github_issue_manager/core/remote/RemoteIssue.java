package com.jw.github_issue_manager.core.remote;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record RemoteIssue(
    PlatformType platform,
    String externalId,
    String repositoryExternalId,
    String numberOrKey,
    String title,
    String body,
    String state,
    String authorLogin,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime closedAt
) {
}
