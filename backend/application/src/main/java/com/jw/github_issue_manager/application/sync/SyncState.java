package com.jw.github_issue_manager.application.sync;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sync_states")
public class SyncState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncResourceType resourceType;

    @Column(nullable = false)
    private String resourceKey;

    @Column(nullable = false)
    private LocalDateTime lastSyncedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncStatus lastSyncStatus;

    @Column(length = 1000)
    private String lastSyncMessage;

    protected SyncState() {
    }

    public SyncState(SyncResourceType resourceType, String resourceKey, LocalDateTime lastSyncedAt, SyncStatus lastSyncStatus, String lastSyncMessage) {
        this.resourceType = resourceType;
        this.resourceKey = resourceKey;
        this.lastSyncedAt = lastSyncedAt;
        this.lastSyncStatus = lastSyncStatus;
        this.lastSyncMessage = lastSyncMessage;
    }

    public void update(LocalDateTime syncedAt, SyncStatus status, String message) {
        this.lastSyncedAt = syncedAt;
        this.lastSyncStatus = status;
        this.lastSyncMessage = message;
    }

    public SyncResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public SyncStatus getLastSyncStatus() {
        return lastSyncStatus;
    }

    public String getLastSyncMessage() {
        return lastSyncMessage;
    }
}
