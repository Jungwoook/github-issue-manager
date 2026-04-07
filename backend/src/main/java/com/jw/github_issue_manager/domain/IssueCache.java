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
@Table(name = "issue_caches", indexes = {
    @Index(name = "idx_issue_repository", columnList = "githubRepositoryId"),
    @Index(name = "idx_issue_repository_number", columnList = "githubRepositoryId,issueNumber", unique = true)
})
public class IssueCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long githubIssueId;

    @Column(nullable = false)
    private Long githubRepositoryId;

    @Column(name = "issueNumber", nullable = false)
    private Integer number;

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
        Long githubIssueId,
        Long githubRepositoryId,
        Integer number,
        String title,
        String body,
        String state,
        String authorLogin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt,
        LocalDateTime lastSyncedAt
    ) {
        this.githubIssueId = githubIssueId;
        this.githubRepositoryId = githubRepositoryId;
        this.number = number;
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

    public Long getGithubIssueId() {
        return githubIssueId;
    }

    public Long getGithubRepositoryId() {
        return githubRepositoryId;
    }

    public Integer getNumber() {
        return number;
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
