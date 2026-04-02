package com.jw.github_issue_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jw.github_issue_manager.domain.IssueCache;

public interface IssueCacheRepository extends JpaRepository<IssueCache, Long> {

    List<IssueCache> findByGithubRepositoryIdOrderByNumberDesc(Long githubRepositoryId);

    Optional<IssueCache> findByGithubRepositoryIdAndNumber(Long githubRepositoryId, Integer number);

    @Query("select coalesce(max(i.number), 0) from IssueCache i where i.githubRepositoryId = :githubRepositoryId")
    int findMaxNumberByGithubRepositoryId(@Param("githubRepositoryId") Long githubRepositoryId);
}
