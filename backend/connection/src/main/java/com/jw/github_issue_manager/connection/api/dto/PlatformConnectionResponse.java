package com.jw.github_issue_manager.connection.api.dto;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record PlatformConnectionResponse(
    PlatformType platform,
    String externalUserId,
    String accountLogin,
    String avatarUrl,
    String tokenScopes,
    String baseUrl,
    LocalDateTime connectedAt,
    LocalDateTime lastAuthenticatedAt
) {
}
