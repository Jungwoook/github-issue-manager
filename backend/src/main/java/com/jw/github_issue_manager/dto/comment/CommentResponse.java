package com.jw.github_issue_manager.dto.comment;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record CommentResponse(
    PlatformType platform,
    String commentId,
    String authorLogin,
    String body,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastSyncedAt
) {
}
