package com.jw.github_issue_manager.core.remote;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record RemoteComment(
    PlatformType platform,
    String externalId,
    String issueExternalId,
    String authorLogin,
    String body,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
