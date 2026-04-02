package com.jw.github_issue_manager.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "github_accounts")
public class GitHubAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private Long githubUserId;

    @Column(nullable = false, unique = true)
    private String login;

    @Column
    private String avatarUrl;

    @Column
    private String accessTokenEncrypted;

    @Column
    private String tokenScopes;

    @Column
    private LocalDateTime tokenVerifiedAt;

    @Column(nullable = false)
    private LocalDateTime connectedAt;

    @Column(nullable = false)
    private LocalDateTime lastAuthenticatedAt;

    protected GitHubAccount() {
    }

    public GitHubAccount(User user, Long githubUserId, String login, String avatarUrl, String accessTokenEncrypted, String tokenScopes) {
        this.user = user;
        this.githubUserId = githubUserId;
        this.login = login;
        this.avatarUrl = avatarUrl;
        this.accessTokenEncrypted = accessTokenEncrypted;
        this.tokenScopes = tokenScopes;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        connectedAt = now;
        lastAuthenticatedAt = now;
    }

    public void touchAuthentication() {
        this.lastAuthenticatedAt = LocalDateTime.now();
    }

    public void markTokenVerified(LocalDateTime verifiedAt) {
        this.tokenVerifiedAt = verifiedAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Long getGithubUserId() {
        return githubUserId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAccessTokenEncrypted() {
        return accessTokenEncrypted;
    }

    public void setAccessTokenEncrypted(String accessTokenEncrypted) {
        this.accessTokenEncrypted = accessTokenEncrypted;
    }

    public String getTokenScopes() {
        return tokenScopes;
    }

    public void setTokenScopes(String tokenScopes) {
        this.tokenScopes = tokenScopes;
    }

    public LocalDateTime getTokenVerifiedAt() {
        return tokenVerifiedAt;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public LocalDateTime getLastAuthenticatedAt() {
        return lastAuthenticatedAt;
    }
}
