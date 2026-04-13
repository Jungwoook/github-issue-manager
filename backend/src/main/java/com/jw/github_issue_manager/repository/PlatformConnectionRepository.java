package com.jw.github_issue_manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.domain.PlatformConnection;

public interface PlatformConnectionRepository extends JpaRepository<PlatformConnection, Long> {

    Optional<PlatformConnection> findByPlatformAndExternalUserId(PlatformType platform, String externalUserId);

    Optional<PlatformConnection> findByPlatformAndUserId(PlatformType platform, Long userId);
}
