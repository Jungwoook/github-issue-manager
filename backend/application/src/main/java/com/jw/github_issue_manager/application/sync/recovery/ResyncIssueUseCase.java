package com.jw.github_issue_manager.application.sync.recovery;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.sync.run.SyncRunResponse;
import com.jw.github_issue_manager.core.platform.PlatformType;

import jakarta.servlet.http.HttpSession;

@Service
public class ResyncIssueUseCase {

    private final SyncRecoveryExecutor syncRecoveryExecutor;

    public ResyncIssueUseCase(SyncRecoveryExecutor syncRecoveryExecutor) {
        this.syncRecoveryExecutor = syncRecoveryExecutor;
    }

    public SyncRunResponse resync(
        String platform,
        String repositoryId,
        String issueNumberOrKey,
        boolean includeComments,
        HttpSession session
    ) {
        PlatformType platformType = PlatformType.from(platform);
        return syncRecoveryExecutor.refreshIssue(platformType, repositoryId, issueNumberOrKey, includeComments, session);
    }
}
