package com.jw.github_issue_manager.core.remote;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record RemoteRepository(
    PlatformType platform,
    String externalId,
    String ownerKey,
    String name,
    String fullName,
    String description,
    boolean isPrivate,
    String webUrl
) {
}
