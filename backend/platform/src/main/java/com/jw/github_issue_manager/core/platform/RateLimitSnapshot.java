package com.jw.github_issue_manager.core.platform;

import java.time.LocalDateTime;

public record RateLimitSnapshot(
    PlatformType platform,
    Integer limit,
    Integer remaining,
    LocalDateTime resetAt,
    Integer retryAfterSeconds,
    String resource,
    LocalDateTime capturedAt
) {
}
