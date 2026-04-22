package com.jw.github_issue_manager.issue.internal.domain;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "issue_caches", indexes = {
    @Index(name = "idx_issue_repository", columnList = "platform,repository_external_id"),
    @Index(name = "idx_issue_repository_number", columnList = "platform,repository_external_id,number_or_key", unique = true)
})
public class IssueCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformType platform;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "repository_external_id", nullable = false)
    private String repositoryExternalId;

    @Column(name = "number_or_key", nullable = false)
    private String numberOrKey;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String body;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String authorLogin;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime closedAt;

    @Column(nullable = false)
    private LocalDateTime lastSyncedAt;

    protected IssueCache() {
    }

    public IssueCache(
        PlatformType platform,
        String externalId,
        String repositoryExternalId,
        String numberOrKey,
        String title,
        String body,
        String state,
        String authorLogin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt,
        LocalDateTime lastSyncedAt
    ) {
        this.platform = platform;
        this.externalId = externalId;
        this.repositoryExternalId = repositoryExternalId;
        this.numberOrKey = numberOrKey;
        this.title = title;
        this.body = body;
        this.state = state;
        this.authorLogin = authorLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
        this.lastSyncedAt = lastSyncedAt;
    }

    public void update(String title, String body, String state, LocalDateTime syncTime) {
        this.title = title;
        this.body = body;
        this.state = state;
        this.updatedAt = syncTime;
        this.closedAt = "CLOSED".equals(state) ? syncTime : null;
        this.lastSyncedAt = syncTime;
    }

    public Long getId() {
        return id;
    }

    public PlatformType getPlatform() {
        return platform;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getRepositoryExternalId() {
        return repositoryExternalId;
    }

    public String getNumberOrKey() {
        return numberOrKey;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getState() {
        return state;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }
}
