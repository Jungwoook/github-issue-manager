package com.jw.github_issue_manager.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(nullable = false, unique = true)
    private Long githubRepositoryId;

    @Column(nullable = false)
    private String ownerLogin;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean isPrivate;

    @Column(nullable = false)
    private String htmlUrl;

    @Column(nullable = false)
    private String defaultBranch;

    @Column
    private LocalDateTime lastPushedAt;

    @Column(nullable = false)
    private LocalDateTime lastSyncedAt;

    protected RepositoryCache() {
    }

    public RepositoryCache(
        Long githubRepositoryId,
        String ownerLogin,
        String name,
        String fullName,
        String description,
        boolean isPrivate,
        String htmlUrl,
        String defaultBranch,
        LocalDateTime lastPushedAt,
        LocalDateTime lastSyncedAt
    ) {
        this.githubRepositoryId = githubRepositoryId;
        this.ownerLogin = ownerLogin;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.isPrivate = isPrivate;
        this.htmlUrl = htmlUrl;
        this.defaultBranch = defaultBranch;
        this.lastPushedAt = lastPushedAt;
        this.lastSyncedAt = lastSyncedAt;
    }

    public void refreshMetadata(
        String description,
        boolean isPrivate,
        String htmlUrl,
        String defaultBranch,
        LocalDateTime lastPushedAt,
        LocalDateTime lastSyncedAt
    ) {
        this.description = description;
        this.isPrivate = isPrivate;
        this.htmlUrl = htmlUrl;
        this.defaultBranch = defaultBranch;
        this.lastPushedAt = lastPushedAt;
        this.lastSyncedAt = lastSyncedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGithubRepositoryId() {
        return githubRepositoryId;
    }

    public String getOwnerLogin() {
        return ownerLogin;
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

    public String getHtmlUrl() {
        return htmlUrl;
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
