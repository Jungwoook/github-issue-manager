package com.jw.github_issue_manager.platform.api.dto;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record CurrentPlatformConnection(
    PlatformType platform,
    Long userId,
    String externalUserId,
    String accountLogin,
    String avatarUrl,
    String tokenScopes,
    String baseUrl
) {
}
