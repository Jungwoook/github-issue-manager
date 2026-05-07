package com.jw.github_issue_manager.application.sync.recovery;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.sync.run.SyncRunResponse;
import com.jw.github_issue_manager.core.platform.PlatformType;

import jakarta.servlet.http.HttpSession;

@Service
public class ResyncRepositoryUseCase {

    private final SyncRecoveryExecutor syncRecoveryExecutor;

    public ResyncRepositoryUseCase(SyncRecoveryExecutor syncRecoveryExecutor) {
        this.syncRecoveryExecutor = syncRecoveryExecutor;
    }

    public SyncRunResponse resync(String platform, String repositoryId, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        return syncRecoveryExecutor.refreshRepositoryIssues(
            platformType,
            repositoryId,
            session,
            "MANUAL_RESYNC",
            "RESYNC_REPOSITORY"
        );
    }
}
