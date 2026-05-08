package com.jw.github_issue_manager.core.platform;

public record PlatformResult<T>(
    T data,
    RateLimitSnapshot rateLimitSnapshot
) {
    public static <T> PlatformResult<T> withoutSnapshot(T data) {
        return new PlatformResult<>(data, null);
    }
}
