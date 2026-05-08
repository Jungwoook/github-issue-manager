package com.jw.github_issue_manager.exception;

import java.time.LocalDateTime;

public record SyncFailureErrorResponse(
    String code,
    String message,
    Long syncRunId,
    Long failureId,
    String status,
    boolean retryable,
    LocalDateTime nextRetryAt,
    LocalDateTime timestamp
) {

    public static SyncFailureErrorResponse of(
        String code,
        String message,
        Long syncRunId,
        Long failureId,
        String status,
        boolean retryable,
        LocalDateTime nextRetryAt
    ) {
        return new SyncFailureErrorResponse(
            code,
            message,
            syncRunId,
            failureId,
            status,
            retryable,
            nextRetryAt,
            LocalDateTime.now()
        );
    }
}
