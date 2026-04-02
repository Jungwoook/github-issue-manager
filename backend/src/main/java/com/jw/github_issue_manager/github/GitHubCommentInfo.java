package com.jw.github_issue_manager.github;

import java.time.LocalDateTime;

public record GitHubCommentInfo(
    Long id,
    String authorLogin,
    String body,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
