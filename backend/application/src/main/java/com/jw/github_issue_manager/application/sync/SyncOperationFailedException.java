package com.jw.github_issue_manager.application.sync;

import java.time.LocalDateTime;

public class SyncOperationFailedException extends RuntimeException {

    private final Long syncRunId;
    private final Long failureId;
    private final String status;
    private final boolean retryable;
    private final LocalDateTime nextRetryAt;

    public SyncOperationFailedException(
        String message,
        Long syncRunId,
        Long failureId,
        String status,
        boolean retryable,
        LocalDateTime nextRetryAt,
        Throwable cause
    ) {
        super(message, cause);
        this.syncRunId = syncRunId;
        this.failureId = failureId;
        this.status = status;
        this.retryable = retryable;
        this.nextRetryAt = nextRetryAt;
    }

    public Long getSyncRunId() {
        return syncRunId;
    }

    public Long getFailureId() {
        return failureId;
    }

    public String getStatus() {
        return status;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }
}
