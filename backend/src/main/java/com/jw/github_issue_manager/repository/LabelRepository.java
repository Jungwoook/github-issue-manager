package com.jw.github_issue_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.domain.Label;
import com.jw.github_issue_manager.domain.RepositoryEntity;

public interface LabelRepository extends JpaRepository<Label, Long> {

    @EntityGraph(attributePaths = {"repository"})
    List<Label> findByRepository(RepositoryEntity repository);

    boolean existsByRepositoryAndName(RepositoryEntity repository, String name);

    @EntityGraph(attributePaths = {"repository"})
    Optional<Label> findByIdAndRepositoryId(Long id, Long repositoryId);
}
