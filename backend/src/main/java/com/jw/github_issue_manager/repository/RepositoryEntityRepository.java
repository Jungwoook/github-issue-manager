package com.jw.github_issue_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.domain.RepositoryEntity;

public interface RepositoryEntityRepository extends JpaRepository<RepositoryEntity, Long> {
}
