package com.jw.github_issue_manager.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "comment_caches", indexes = {
    @Index(name = "idx_comment_issue", columnList = "githubIssueId")
})
public class CommentCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long githubCommentId;

    @Column(nullable = false)
    private Long githubIssueId;

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
        Long githubCommentId,
        Long githubIssueId,
        String authorLogin,
        String body,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastSyncedAt
    ) {
        this.githubCommentId = githubCommentId;
        this.githubIssueId = githubIssueId;
        this.authorLogin = authorLogin;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastSyncedAt = lastSyncedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGithubCommentId() {
        return githubCommentId;
    }

    public Long getGithubIssueId() {
        return githubIssueId;
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
