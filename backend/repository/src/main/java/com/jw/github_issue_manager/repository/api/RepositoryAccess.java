package com.jw.github_issue_manager.repository.api;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record RepositoryAccess(
    PlatformType platform,
    String externalId,
    String ownerKey,
    String name
) {
}
