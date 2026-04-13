package com.jw.github_issue_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.domain.IssueCache;

public interface IssueCacheRepository extends JpaRepository<IssueCache, Long> {

    @Query("""
        select i
        from IssueCache i
        where i.platform = :platform and i.repositoryExternalId = :repositoryExternalId
        order by cast(i.numberOrKey as integer) desc
        """)
    List<IssueCache> findByPlatformAndRepositoryExternalIdOrderByNumberOrKeyDesc(
        @Param("platform") PlatformType platform,
        @Param("repositoryExternalId") String repositoryExternalId
    );

    @Query("""
        select i
        from IssueCache i
        where i.platform = :platform
          and i.repositoryExternalId = :repositoryExternalId
          and i.numberOrKey = :numberOrKey
        """)
    Optional<IssueCache> findByPlatformAndRepositoryExternalIdAndNumberOrKey(
        @Param("platform") PlatformType platform,
        @Param("repositoryExternalId") String repositoryExternalId,
        @Param("numberOrKey") String numberOrKey
    );

    @Query("""
        select coalesce(max(cast(i.numberOrKey as integer)), 0)
        from IssueCache i
        where i.platform = :platform and i.repositoryExternalId = :repositoryExternalId
        """)
    int findMaxNumberByPlatformAndRepositoryExternalId(
        @Param("platform") PlatformType platform,
        @Param("repositoryExternalId") String repositoryExternalId
    );
}
