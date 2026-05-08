package com.jw.github_issue_manager.application.ratelimit;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.core.platform.PlatformType;

public interface RateLimitSnapshotRepository extends JpaRepository<RateLimitSnapshot, Long> {

    Optional<RateLimitSnapshot> findTopByPlatformOrderByCapturedAtDesc(PlatformType platform);
}
