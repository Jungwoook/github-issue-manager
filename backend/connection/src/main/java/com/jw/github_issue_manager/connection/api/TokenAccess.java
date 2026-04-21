package com.jw.github_issue_manager.connection.api;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record TokenAccess(
    PlatformType platform,
    String accessToken,
    String baseUrl,
    String accountLogin
) {
}
