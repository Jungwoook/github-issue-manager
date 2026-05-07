package com.jw.github_issue_manager.application.sync.failure;

import java.time.LocalDateTime;

public record SyncFailureResponse(
    Long id,
    Long syncRunId,
    String platform,
    String resourceType,
    String resourceKey,
    String operation,
    String errorType,
    boolean retryable,
    int retryCount,
    LocalDateTime nextRetryAt,
    String lastErrorMessage,
    LocalDateTime resolvedAt,
    LocalDateTime createdAt
) {
}
