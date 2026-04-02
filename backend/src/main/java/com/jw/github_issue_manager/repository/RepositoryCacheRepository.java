package com.jw.github_issue_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.domain.RepositoryCache;

public interface RepositoryCacheRepository extends JpaRepository<RepositoryCache, Long> {

    List<RepositoryCache> findByOwnerLoginOrderByFullNameAsc(String ownerLogin);

    Optional<RepositoryCache> findByGithubRepositoryId(Long githubRepositoryId);
}
