package com.jw.github_issue_manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jw.github_issue_manager.domain.CommentCache;

public interface CommentCacheRepository extends JpaRepository<CommentCache, Long> {

    List<CommentCache> findByGithubIssueIdOrderByCreatedAtAsc(Long githubIssueId);

    @Query("select coalesce(max(c.githubCommentId), 0) from CommentCache c where c.githubIssueId = :githubIssueId")
    long findMaxGithubCommentIdByGithubIssueId(@Param("githubIssueId") Long githubIssueId);
}
