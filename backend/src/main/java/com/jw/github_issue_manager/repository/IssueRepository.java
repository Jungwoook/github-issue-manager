package com.jw.github_issue_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.domain.Issue;
import com.jw.github_issue_manager.domain.RepositoryEntity;
import com.jw.github_issue_manager.domain.User;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    @EntityGraph(attributePaths = {"assignee", "labels", "repository"})
    List<Issue> findByRepository(RepositoryEntity repository);

    @EntityGraph(attributePaths = {"assignee", "labels", "repository"})
    Optional<Issue> findByIdAndRepositoryId(Long id, Long repositoryId);

    boolean existsByAssignee(User assignee);
}
