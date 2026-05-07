package com.jw.github_issue_manager.application.ratelimit;

import java.time.LocalDateTime;

public record RateLimitSnapshotResponse(
    String platform,
    Integer limit,
    Integer remaining,
    LocalDateTime resetAt,
    Integer retryAfterSeconds,
    String resource,
    LocalDateTime capturedAt
) {
}
