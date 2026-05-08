package com.jw.github_issue_manager.application.sync.failure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.application.sync.run.SyncRun;

@Service
public class SyncFailureService {

    private final SyncFailureRepository syncFailureRepository;

    public SyncFailureService(SyncFailureRepository syncFailureRepository) {
        this.syncFailureRepository = syncFailureRepository;
    }

    @Transactional
    public SyncFailure recordFailure(
        SyncRun syncRun,
        String operation,
        SyncFailureType errorType,
        boolean retryable,
        LocalDateTime nextRetryAt,
        String message
    ) {
        return syncFailureRepository.save(SyncFailure.create(
            syncRun.getId(),
            syncRun.getPlatform(),
            syncRun.getResourceType(),
            syncRun.getResourceKey(),
            operation,
            errorType,
            retryable,
            nextRetryAt,
            message
        ));
    }

    @Transactional(readOnly = true)
    public List<SyncFailureResponse> getOpenFailures() {
        return syncFailureRepository.findTop50ByResolvedAtIsNullOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SyncFailure requireFailure(Long id) {
        return syncFailureRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Sync failure not found: " + id));
    }

    @Transactional
    public void markResolved(SyncFailure failure) {
        failure.markResolved();
        syncFailureRepository.save(failure);
    }

    @Transactional
    public void recordRetryFailure(SyncFailure failure, LocalDateTime nextRetryAt, String message) {
        failure.recordRetryFailure(nextRetryAt, message);
        syncFailureRepository.save(failure);
    }

    public SyncFailureResponse toResponse(SyncFailure failure) {
        return new SyncFailureResponse(
            failure.getId(),
            failure.getSyncRunId(),
            failure.getPlatform().name(),
            failure.getResourceType().name(),
            failure.getResourceKey(),
            failure.getOperation(),
            failure.getErrorType().name(),
            failure.isRetryable(),
            failure.getRetryCount(),
            failure.getNextRetryAt(),
            failure.getLastErrorMessage(),
            failure.getResolvedAt(),
            failure.getCreatedAt()
        );
    }
}
