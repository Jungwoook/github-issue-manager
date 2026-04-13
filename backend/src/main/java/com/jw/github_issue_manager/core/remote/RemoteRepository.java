package com.jw.github_issue_manager.core.remote;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record RemoteRepository(
    PlatformType platform,
    String externalId,
    String ownerKey,
    String name,
    String fullName,
    String description,
    boolean isPrivate,
    String webUrl,
    String defaultBranch,
    LocalDateTime pushedAt
) {
}
