package com.jw.github_issue_manager.connection.api.dto;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record MeResponse(
    Long id,
    String displayName,
    PlatformType platform,
    String accountLogin,
    String avatarUrl
) {
}
