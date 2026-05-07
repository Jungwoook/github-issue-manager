package com.jw.github_issue_manager.application.sync.recovery;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.sync.run.SyncRunResponse;
import com.jw.github_issue_manager.application.sync.run.SyncRunService;

@Service
public class GetSyncRunsUseCase {

    private final SyncRunService syncRunService;

    public GetSyncRunsUseCase(SyncRunService syncRunService) {
        this.syncRunService = syncRunService;
    }

    public List<SyncRunResponse> getRecentRuns() {
        return syncRunService.getRecentRuns();
    }

    public SyncRunResponse getRun(Long syncRunId) {
        return syncRunService.getRun(syncRunId);
    }
}
