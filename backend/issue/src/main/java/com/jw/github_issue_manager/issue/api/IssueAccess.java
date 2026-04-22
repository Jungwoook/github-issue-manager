package com.jw.github_issue_manager.issue.api;

import com.jw.github_issue_manager.core.platform.PlatformType;

public record IssueAccess(
    PlatformType platform,
    String externalId,
    String repositoryExternalId,
    String numberOrKey
) {
}
