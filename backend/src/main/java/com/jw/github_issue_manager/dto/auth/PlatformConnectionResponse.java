package com.jw.github_issue_manager.dto.auth;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record PlatformConnectionResponse(
    PlatformType platform,
    String externalUserId,
    String accountLogin,
    String avatarUrl,
    String tokenScopes,
    LocalDateTime connectedAt,
    LocalDateTime lastAuthenticatedAt
) {
}
