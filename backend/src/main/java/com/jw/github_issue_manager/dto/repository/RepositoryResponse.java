package com.jw.github_issue_manager.dto.repository;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record RepositoryResponse(
    PlatformType platform,
    String repositoryId,
    String ownerKey,
    String name,
    String fullName,
    String description,
    String webUrl,
    boolean isPrivate,
    LocalDateTime lastSyncedAt
) {
}
