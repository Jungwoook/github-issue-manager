package com.jw.github_issue_manager.application.sync.failure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncFailureRepository extends JpaRepository<SyncFailure, Long> {

    List<SyncFailure> findTop50ByResolvedAtIsNullOrderByCreatedAtDesc();
}
