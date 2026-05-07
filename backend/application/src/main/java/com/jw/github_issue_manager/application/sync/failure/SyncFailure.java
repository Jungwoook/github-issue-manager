package com.jw.github_issue_manager.application.sync.failure;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.application.sync.SyncResourceType;
import com.jw.github_issue_manager.core.platform.PlatformType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sync_failures")
public class SyncFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long syncRunId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformType platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncResourceType resourceType;

    @Column(nullable = false)
    private String resourceKey;

    @Column(nullable = false)
    private String operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncFailureType errorType;

    @Column(nullable = false)
    private boolean retryable;

    @Column(nullable = false)
    private int retryCount;

    private LocalDateTime nextRetryAt;

    @Column(length = 1000)
    private String lastErrorMessage;

    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected SyncFailure() {
    }

    private SyncFailure(
        Long syncRunId,
        PlatformType platform,
        SyncResourceType resourceType,
        String resourceKey,
        String operation,
        SyncFailureType errorType,
        boolean retryable,
        LocalDateTime nextRetryAt,
        String lastErrorMessage
    ) {
        this.syncRunId = syncRunId;
        this.platform = platform;
        this.resourceType = resourceType;
        this.resourceKey = resourceKey;
        this.operation = operation;
        this.errorType = errorType;
        this.retryable = retryable;
        this.nextRetryAt = nextRetryAt;
        this.lastErrorMessage = lastErrorMessage;
        this.createdAt = LocalDateTime.now();
    }

    public static SyncFailure create(
        Long syncRunId,
        PlatformType platform,
        SyncResourceType resourceType,
        String resourceKey,
        String operation,
        SyncFailureType errorType,
        boolean retryable,
        LocalDateTime nextRetryAt,
        String lastErrorMessage
    ) {
        return new SyncFailure(syncRunId, platform, resourceType, resourceKey, operation, errorType, retryable, nextRetryAt, lastErrorMessage);
    }

    public void markResolved() {
        this.resolvedAt = LocalDateTime.now();
    }

    public void recordRetryFailure(LocalDateTime nextRetryAt, String message) {
        this.retryCount++;
        this.nextRetryAt = nextRetryAt;
        this.lastErrorMessage = message;
    }

    public Long getId() {
        return id;
    }

    public Long getSyncRunId() {
        return syncRunId;
    }

    public PlatformType getPlatform() {
        return platform;
    }

    public SyncResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public String getOperation() {
        return operation;
    }

    public SyncFailureType getErrorType() {
        return errorType;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
