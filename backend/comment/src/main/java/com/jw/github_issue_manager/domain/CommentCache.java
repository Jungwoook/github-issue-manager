package com.jw.github_issue_manager.domain;

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
@Table(name = "comment_caches", indexes = {
    @Index(name = "idx_comment_issue", columnList = "platform,issue_external_id")
})
public class CommentCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformType platform;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "issue_external_id", nullable = false)
    private String issueExternalId;

    @Column(nullable = false)
    private String authorLogin;

    @Column(nullable = false, length = 5000)
    private String body;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime lastSyncedAt;

    protected CommentCache() {
    }

    public CommentCache(
        PlatformType platform,
        String externalId,
        String issueExternalId,
        String authorLogin,
        String body,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastSyncedAt
    ) {
        this.platform = platform;
        this.externalId = externalId;
        this.issueExternalId = issueExternalId;
        this.authorLogin = authorLogin;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getIssueExternalId() {
        return issueExternalId;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }
}
