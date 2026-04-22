package com.jw.github_issue_manager.repository.internal.domain;

import java.time.LocalDateTime;

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
@Table(name = "repository_caches")
public class RepositoryCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformType platform;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "owner_key", nullable = false)
    private String ownerKey;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean isPrivate;

    @Column(name = "web_url", nullable = false)
    private String webUrl;

    @Column(nullable = false)
    private String defaultBranch;

    @Column
    private LocalDateTime lastPushedAt;

    @Column(nullable = false)
    private LocalDateTime lastSyncedAt;

    protected RepositoryCache() {
    }

    public RepositoryCache(
        PlatformType platform,
        String externalId,
        String ownerKey,
        String name,
        String fullName,
        String description,
        boolean isPrivate,
        String webUrl,
        String defaultBranch,
        LocalDateTime lastPushedAt,
        LocalDateTime lastSyncedAt
    ) {
        this.platform = platform;
        this.externalId = externalId;
        this.ownerKey = ownerKey;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.isPrivate = isPrivate;
        this.webUrl = webUrl;
        this.defaultBranch = defaultBranch;
        this.lastPushedAt = lastPushedAt;
        this.lastSyncedAt = lastSyncedAt;
    }

    public void refreshMetadata(
        String description,
        boolean isPrivate,
        String webUrl,
        String defaultBranch,
        LocalDateTime lastPushedAt,
        LocalDateTime lastSyncedAt
    ) {
        this.description = description;
        this.isPrivate = isPrivate;
        this.webUrl = webUrl;
        this.defaultBranch = defaultBranch;
        this.lastPushedAt = lastPushedAt;
        this.lastSyncedAt = lastSyncedAt;
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

    public String getOwnerKey() {
        return ownerKey;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public LocalDateTime getLastPushedAt() {
        return lastPushedAt;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }
}
