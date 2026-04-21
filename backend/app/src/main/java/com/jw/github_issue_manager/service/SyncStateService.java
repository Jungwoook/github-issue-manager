package com.jw.github_issue_manager.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.domain.SyncResourceType;
import com.jw.github_issue_manager.domain.SyncState;
import com.jw.github_issue_manager.domain.SyncStatus;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.repository.SyncStateRepository;

@Service
public class SyncStateService {

    private final SyncStateRepository syncStateRepository;

    public SyncStateService(SyncStateRepository syncStateRepository) {
        this.syncStateRepository = syncStateRepository;
    }

    @Transactional
    public SyncState recordSuccess(SyncResourceType resourceType, String resourceKey, String message) {
        return upsert(resourceType, resourceKey, SyncStatus.SUCCESS, message);
    }

    @Transactional(readOnly = true)
    public SyncStateResponse getSyncState(SyncResourceType resourceType, String resourceKey) {
        return syncStateRepository.findByResourceTypeAndResourceKey(resourceType, resourceKey)
            .map(this::toResponse)
            .orElse(null);
    }

    private SyncState upsert(SyncResourceType resourceType, String resourceKey, SyncStatus status, String message) {
        LocalDateTime now = LocalDateTime.now();
        SyncState state = syncStateRepository.findByResourceTypeAndResourceKey(resourceType, resourceKey)
            .map(existing -> {
                existing.update(now, status, message);
                return existing;
            })
            .orElseGet(() -> new SyncState(resourceType, resourceKey, now, status, message));

        return syncStateRepository.save(state);
    }

    private SyncStateResponse toResponse(SyncState syncState) {
        return new SyncStateResponse(
            syncState.getResourceType().name(),
            syncState.getResourceKey(),
            syncState.getLastSyncedAt(),
            syncState.getLastSyncStatus().name(),
            syncState.getLastSyncMessage()
        );
    }
}
