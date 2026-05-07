package com.jw.github_issue_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.application.sync.failure.SyncFailureResponse;
import com.jw.github_issue_manager.application.sync.recovery.GetSyncFailuresUseCase;
import com.jw.github_issue_manager.application.sync.recovery.GetSyncRunsUseCase;
import com.jw.github_issue_manager.application.sync.recovery.ResyncIssueUseCase;
import com.jw.github_issue_manager.application.sync.recovery.ResyncRepositoryUseCase;
import com.jw.github_issue_manager.application.sync.recovery.RetrySyncFailureUseCase;
import com.jw.github_issue_manager.application.sync.run.SyncRunResponse;

import jakarta.servlet.http.HttpSession;

@RestController
public class SyncRecoveryController {

    private final GetSyncFailuresUseCase getSyncFailuresUseCase;
    private final GetSyncRunsUseCase getSyncRunsUseCase;
    private final RetrySyncFailureUseCase retrySyncFailureUseCase;
    private final ResyncRepositoryUseCase resyncRepositoryUseCase;
    private final ResyncIssueUseCase resyncIssueUseCase;

    public SyncRecoveryController(
        GetSyncFailuresUseCase getSyncFailuresUseCase,
        GetSyncRunsUseCase getSyncRunsUseCase,
        RetrySyncFailureUseCase retrySyncFailureUseCase,
        ResyncRepositoryUseCase resyncRepositoryUseCase,
        ResyncIssueUseCase resyncIssueUseCase
    ) {
        this.getSyncFailuresUseCase = getSyncFailuresUseCase;
        this.getSyncRunsUseCase = getSyncRunsUseCase;
        this.retrySyncFailureUseCase = retrySyncFailureUseCase;
        this.resyncRepositoryUseCase = resyncRepositoryUseCase;
        this.resyncIssueUseCase = resyncIssueUseCase;
    }

    @GetMapping("/api/sync-failures")
    public ResponseEntity<List<SyncFailureResponse>> getSyncFailures() {
        return ResponseEntity.ok(getSyncFailuresUseCase.getOpenFailures());
    }

    @PostMapping("/api/sync-failures/{failureId}/retry")
    public ResponseEntity<SyncRunResponse> retrySyncFailure(
        @PathVariable Long failureId,
        HttpSession session
    ) {
        return ResponseEntity.ok(retrySyncFailureUseCase.retry(failureId, session));
    }

    @GetMapping("/api/sync-runs")
    public ResponseEntity<List<SyncRunResponse>> getSyncRuns() {
        return ResponseEntity.ok(getSyncRunsUseCase.getRecentRuns());
    }

    @GetMapping("/api/sync-runs/{syncRunId}")
    public ResponseEntity<SyncRunResponse> getSyncRun(@PathVariable Long syncRunId) {
        return ResponseEntity.ok(getSyncRunsUseCase.getRun(syncRunId));
    }

    @PostMapping("/api/platforms/{platform}/repositories/{repositoryId}/resync")
    public ResponseEntity<SyncRunResponse> resyncRepository(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        HttpSession session
    ) {
        return ResponseEntity.ok(resyncRepositoryUseCase.resync(platform, repositoryId, session));
    }

    @PostMapping("/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/resync")
    public ResponseEntity<SyncRunResponse> resyncIssue(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @PathVariable String issueNumberOrKey,
        @RequestParam(defaultValue = "false") boolean includeComments,
        HttpSession session
    ) {
        return ResponseEntity.ok(resyncIssueUseCase.resync(platform, repositoryId, issueNumberOrKey, includeComments, session));
    }
}
