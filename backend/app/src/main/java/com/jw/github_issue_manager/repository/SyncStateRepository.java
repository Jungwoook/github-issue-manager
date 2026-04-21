package com.jw.github_issue_manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.domain.SyncResourceType;
import com.jw.github_issue_manager.domain.SyncState;

public interface SyncStateRepository extends JpaRepository<SyncState, Long> {

    Optional<SyncState> findByResourceTypeAndResourceKey(SyncResourceType resourceType, String resourceKey);
}
