package com.jw.github_issue_manager.application.ratelimit;

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
@Table(name = "platform_rate_limit_snapshots")
public class RateLimitSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformType platform;

    private Integer limitCount;

    private Integer remainingCount;

    private LocalDateTime resetAt;

    private Integer retryAfterSeconds;

    private String resource;

    @Column(nullable = false)
    private LocalDateTime capturedAt;

    protected RateLimitSnapshot() {
    }

    public RateLimitSnapshot(
        PlatformType platform,
        Integer limitCount,
        Integer remainingCount,
        LocalDateTime resetAt,
        Integer retryAfterSeconds,
        String resource,
        LocalDateTime capturedAt
    ) {
        this.platform = platform;
        this.limitCount = limitCount;
        this.remainingCount = remainingCount;
        this.resetAt = resetAt;
        this.retryAfterSeconds = retryAfterSeconds;
        this.resource = resource;
        this.capturedAt = capturedAt;
    }

    public Long getId() {
        return id;
    }

    public PlatformType getPlatform() {
        return platform;
    }

    public Integer getLimitCount() {
        return limitCount;
    }

    public Integer getRemainingCount() {
        return remainingCount;
    }

    public LocalDateTime getResetAt() {
        return resetAt;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public String getResource() {
        return resource;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }
}
