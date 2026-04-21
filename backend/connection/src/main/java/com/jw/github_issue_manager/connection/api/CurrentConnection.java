package com.jw.github_issue_manager.connection.api;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record CurrentConnection(
    PlatformType platform,
    Long userId,
    String externalUserId,
    String accountLogin,
    String avatarUrl,
    String tokenScopes,
    String baseUrl
) {
}
