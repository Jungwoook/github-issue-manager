package com.jw.github_issue_manager.dto.auth;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record PlatformTokenStatusResponse(
    PlatformType platform,
    boolean connected,
    String accountLogin,
    String tokenScopes,
    String baseUrl,
    LocalDateTime tokenVerifiedAt
) {
}
