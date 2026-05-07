package com.jw.github_issue_manager.application.sync.run;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.application.sync.SyncResourceType;
import com.jw.github_issue_manager.core.platform.PlatformType;

@Service
public class SyncRunService {

    private final SyncRunRepository syncRunRepository;

    public SyncRunService(SyncRunRepository syncRunRepository) {
        this.syncRunRepository = syncRunRepository;
    }

    @Transactional
    public SyncRun start(PlatformType platform, SyncResourceType resourceType, String resourceKey, String triggerType) {
        return syncRunRepository.save(SyncRun.start(platform, resourceType, resourceKey, triggerType));
    }

    @Transactional
    public SyncRun completeSuccess(SyncRun syncRun, int updatedCount) {
        syncRun.complete(SyncRunStatus.SUCCESS, 0, updatedCount, 0, 0, null);
        return syncRunRepository.save(syncRun);
    }

    @Transactional
    public SyncRun completeFailed(SyncRun syncRun, String message) {
        syncRun.complete(SyncRunStatus.FAILED, 0, 0, 0, 1, message);
        return syncRunRepository.save(syncRun);
    }

    @Transactional
    public SyncRun completeRateLimited(SyncRun syncRun, String message) {
        syncRun.complete(SyncRunStatus.RATE_LIMITED, 0, 0, 0, 1, message);
        return syncRunRepository.save(syncRun);
    }

    @Transactional(readOnly = true)
    public List<SyncRunResponse> getRecentRuns() {
        return syncRunRepository.findTop50ByOrderByStartedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SyncRunResponse getRun(Long id) {
        return syncRunRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new IllegalArgumentException("Sync run not found: " + id));
    }

    public SyncRunResponse toResponse(SyncRun syncRun) {
        return new SyncRunResponse(
            syncRun.getId(),
            syncRun.getPlatform().name(),
            syncRun.getResourceType().name(),
            syncRun.getResourceKey(),
            syncRun.getStatus().name(),
            syncRun.getTriggerType(),
            syncRun.getStartedAt(),
            syncRun.getFinishedAt(),
            syncRun.getCreatedCount(),
            syncRun.getUpdatedCount(),
            syncRun.getSkippedCount(),
            syncRun.getFailedCount(),
            syncRun.getFailureMessage()
        );
    }
}
