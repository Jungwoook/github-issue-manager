package com.jw.github_issue_manager.application.sync.recovery;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.sync.failure.SyncFailureResponse;
import com.jw.github_issue_manager.application.sync.failure.SyncFailureService;

@Service
public class GetSyncFailuresUseCase {

    private final SyncFailureService syncFailureService;

    public GetSyncFailuresUseCase(SyncFailureService syncFailureService) {
        this.syncFailureService = syncFailureService;
    }

    public List<SyncFailureResponse> getOpenFailures() {
        return syncFailureService.getOpenFailures();
    }
}
