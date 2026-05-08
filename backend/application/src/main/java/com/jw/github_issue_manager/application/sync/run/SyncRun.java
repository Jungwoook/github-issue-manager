package com.jw.github_issue_manager.application.sync.run;

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
@Table(name = "sync_runs")
public class SyncRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformType platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncResourceType resourceType;

    @Column(nullable = false)
    private String resourceKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncRunStatus status;

    @Column(nullable = false)
    private String triggerType;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private int createdCount;

    private int updatedCount;

    private int skippedCount;

    private int failedCount;

    @Column(length = 1000)
    private String failureMessage;

    protected SyncRun() {
    }

    private SyncRun(PlatformType platform, SyncResourceType resourceType, String resourceKey, String triggerType) {
        this.platform = platform;
        this.resourceType = resourceType;
        this.resourceKey = resourceKey;
        this.triggerType = triggerType;
        this.status = SyncRunStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public static SyncRun start(PlatformType platform, SyncResourceType resourceType, String resourceKey, String triggerType) {
        return new SyncRun(platform, resourceType, resourceKey, triggerType);
    }

    public void complete(SyncRunStatus status, int createdCount, int updatedCount, int skippedCount, int failedCount, String failureMessage) {
        this.status = status;
        this.createdCount = createdCount;
        this.updatedCount = updatedCount;
        this.skippedCount = skippedCount;
        this.failedCount = failedCount;
        this.failureMessage = failureMessage;
        this.finishedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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

    public SyncRunStatus getStatus() {
        return status;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public String getFailureMessage() {
        return failureMessage;
    }
}
