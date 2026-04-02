package com.jw.github_issue_manager.dto.comment;

import java.time.LocalDateTime;

public record CommentResponse(
    Long githubCommentId,
    String authorLogin,
    String body,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastSyncedAt
) {
}
