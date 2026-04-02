package com.jw.github_issue_manager.dto.repository;

import java.time.LocalDateTime;

public record RepositoryResponse(
    Long githubRepositoryId,
    String ownerLogin,
    String name,
    String fullName,
    String description,
    String htmlUrl,
    boolean isPrivate,
    LocalDateTime lastSyncedAt
) {
}
