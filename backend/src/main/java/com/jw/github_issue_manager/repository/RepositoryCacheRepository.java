package com.jw.github_issue_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.domain.RepositoryCache;

public interface RepositoryCacheRepository extends JpaRepository<RepositoryCache, Long> {

    List<RepositoryCache> findByPlatformAndOwnerKeyOrderByFullNameAsc(PlatformType platform, String ownerKey);

    Optional<RepositoryCache> findByPlatformAndExternalId(PlatformType platform, String externalId);
}
