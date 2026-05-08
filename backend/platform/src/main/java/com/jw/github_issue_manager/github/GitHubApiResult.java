package com.jw.github_issue_manager.github;

import com.jw.github_issue_manager.core.platform.RateLimitSnapshot;

public record GitHubApiResult<T>(
    T data,
    RateLimitSnapshot rateLimitSnapshot
) {
}
