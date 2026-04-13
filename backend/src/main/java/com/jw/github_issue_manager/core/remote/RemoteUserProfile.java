package com.jw.github_issue_manager.core.remote;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record RemoteUserProfile(
    PlatformType platform,
    String externalUserId,
    String login,
    String displayName,
    String email,
    String avatarUrl
) {
}
