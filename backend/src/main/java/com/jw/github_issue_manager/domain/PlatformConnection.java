package com.jw.github_issue_manager.domain;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.core.platform.PlatformType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "platform_connections")
public class PlatformConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformType platform;

    @Column(name = "external_user_id", nullable = false, unique = true)
    private String externalUserId;

    @Column(name = "account_login", nullable = false, unique = true)
    private String accountLogin;

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

    protected PlatformConnection() {
    }

    public PlatformConnection(
        User user,
        PlatformType platform,
        String externalUserId,
        String accountLogin,
        String avatarUrl,
        String accessTokenEncrypted,
        String tokenScopes
    ) {
        this.user = user;
        this.platform = platform;
        this.externalUserId = externalUserId;
        this.accountLogin = accountLogin;
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

    public PlatformType getPlatform() {
        return platform;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public String getAccountLogin() {
        return accountLogin;
    }

    public void setAccountLogin(String accountLogin) {
        this.accountLogin = accountLogin;
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
