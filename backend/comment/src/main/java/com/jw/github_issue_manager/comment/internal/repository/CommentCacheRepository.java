package com.jw.github_issue_manager.comment.internal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.comment.internal.domain.CommentCache;

public interface CommentCacheRepository extends JpaRepository<CommentCache, Long> {

    List<CommentCache> findByPlatformAndIssueExternalIdOrderByCreatedAtAsc(PlatformType platform, String issueExternalId);

    java.util.Optional<CommentCache> findByPlatformAndExternalId(PlatformType platform, String externalId);

    @Query("""
        select coalesce(max(cast(c.externalId as integer)), 0)
        from CommentCache c
        where c.platform = :platform and c.issueExternalId = :issueExternalId
        """)
    long findMaxExternalIdByPlatformAndIssueExternalId(
        @Param("platform") PlatformType platform,
        @Param("issueExternalId") String issueExternalId
    );
}
