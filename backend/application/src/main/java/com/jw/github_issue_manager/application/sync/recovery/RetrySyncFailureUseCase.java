package com.jw.github_issue_manager.application.sync.recovery;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.sync.SyncOperationFailedException;
import com.jw.github_issue_manager.application.sync.failure.SyncFailure;
import com.jw.github_issue_manager.application.sync.failure.SyncFailureService;
import com.jw.github_issue_manager.application.sync.run.SyncRunResponse;
import com.jw.github_issue_manager.core.platform.PlatformType;

import jakarta.servlet.http.HttpSession;

@Service
public class RetrySyncFailureUseCase {

    private final SyncFailureService syncFailureService;
    private final SyncRecoveryExecutor syncRecoveryExecutor;

    public RetrySyncFailureUseCase(
        SyncFailureService syncFailureService,
        SyncRecoveryExecutor syncRecoveryExecutor
    ) {
        this.syncFailureService = syncFailureService;
        this.syncRecoveryExecutor = syncRecoveryExecutor;
    }

    public SyncRunResponse retry(Long failureId, HttpSession session) {
        SyncFailure failure = syncFailureService.requireFailure(failureId);
        validateRetryable(failure);

        try {
            SyncRunResponse response = switch (failure.getOperation()) {
                case "REFRESH_REPOSITORIES" -> syncRecoveryExecutor.refreshRepositories(
                    failure.getPlatform(),
                    session,
                    "MANUAL_RETRY",
                    "RETRY_REFRESH_REPOSITORIES"
                );
                case "REFRESH_ISSUES", "RESYNC_REPOSITORY" -> syncRecoveryExecutor.refreshRepositoryIssues(
                    failure.getPlatform(),
                    repositoryId(failure.getPlatform(), failure.getResourceKey()),
                    session,
                    "MANUAL_RETRY",
                    "RETRY_REFRESH_ISSUES"
                );
                case "RESYNC_ISSUE" -> {
                    String[] parts = resourceParts(failure.getPlatform(), failure.getResourceKey(), 2);
                    yield syncRecoveryExecutor.refreshIssue(failure.getPlatform(), parts[0], parts[1], false, session);
                }
                default -> throw new IllegalArgumentException("Unsupported retry operation: " + failure.getOperation());
            };
            syncFailureService.markResolved(failure);
            return response;
        } catch (SyncOperationFailedException exception) {
            syncFailureService.recordRetryFailure(failure, exception.getNextRetryAt(), exception.getMessage());
            throw exception;
        }
    }

    private void validateRetryable(SyncFailure failure) {
        if (failure.getResolvedAt() != null) {
            throw new IllegalArgumentException("Sync failure is already resolved: " + failure.getId());
        }
        if (!failure.isRetryable()) {
            throw new IllegalArgumentException("Sync failure is not retryable: " + failure.getId());
        }
        if (failure.getNextRetryAt() != null && failure.getNextRetryAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Sync failure can be retried after: " + failure.getNextRetryAt());
        }
    }

    private String repositoryId(PlatformType platform, String resourceKey) {
        return resourceParts(platform, resourceKey, 1)[0];
    }

    private String[] resourceParts(PlatformType platform, String resourceKey, int expectedCount) {
        String prefix = platform.name() + ":";
        if (!resourceKey.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid sync failure resource key: " + resourceKey);
        }
        String[] parts = resourceKey.substring(prefix.length()).split(":");
        if (parts.length != expectedCount) {
            throw new IllegalArgumentException("Invalid sync failure resource key: " + resourceKey);
        }
        return parts;
    }
}
