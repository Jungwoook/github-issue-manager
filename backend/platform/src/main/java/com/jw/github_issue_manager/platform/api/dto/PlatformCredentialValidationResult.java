package com.jw.github_issue_manager.platform.api.dto;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record PlatformCredentialValidationResult(
    PlatformType platform,
    String externalUserId,
    String login,
    String displayName,
    String email,
    String avatarUrl,
    String baseUrl
) {
}
