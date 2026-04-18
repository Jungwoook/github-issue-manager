package com.jw.github_issue_manager.gitlab;

import java.time.LocalDateTime;

public record GitLabCommentInfo(
    Long id,
    String authorLogin,
    String body,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
