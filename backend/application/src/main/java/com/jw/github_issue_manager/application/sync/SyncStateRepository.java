package com.jw.github_issue_manager.application.sync;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncStateRepository extends JpaRepository<SyncState, Long> {

    Optional<SyncState> findByResourceTypeAndResourceKey(SyncResourceType resourceType, String resourceKey);
}
