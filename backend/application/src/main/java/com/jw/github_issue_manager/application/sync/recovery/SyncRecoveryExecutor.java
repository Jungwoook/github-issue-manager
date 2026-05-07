package com.jw.github_issue_manager.application.sync.recovery;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jw.github_issue_manager.application.ratelimit.RateLimitService;
import com.jw.github_issue_manager.application.sync.SyncFailureClassifier;
import com.jw.github_issue_manager.application.sync.SyncOperationFailedException;
import com.jw.github_issue_manager.application.sync.SyncResourceType;
import com.jw.github_issue_manager.application.sync.SyncStateService;
import com.jw.github_issue_manager.application.sync.failure.SyncFailure;
import com.jw.github_issue_manager.application.sync.failure.SyncFailureService;
import com.jw.github_issue_manager.application.sync.run.SyncRun;
import com.jw.github_issue_manager.application.sync.run.SyncRunResponse;
import com.jw.github_issue_manager.application.sync.run.SyncRunService;
import com.jw.github_issue_manager.application.sync.run.SyncRunStatus;
import com.jw.github_issue_manager.comment.api.CommentFacade;
import com.jw.github_issue_manager.connection.api.CurrentConnection;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.TokenAccess;
import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.issue.api.IssueFacade;
import com.jw.github_issue_manager.repository.api.RepositoryAccess;
import com.jw.github_issue_manager.repository.api.RepositoryFacade;

import jakarta.servlet.http.HttpSession;

@Component
class SyncRecoveryExecutor {

    private final RepositoryFacade repositoryFacade;
    private final IssueFacade issueFacade;
    private final CommentFacade commentFacade;
    private final PlatformConnectionFacade platformConnectionFacade;
    private final PlatformGatewayResolver platformGatewayResolver;
    private final SyncStateService syncStateService;
    private final SyncRunService syncRunService;
    private final SyncFailureService syncFailureService;
    private final SyncFailureClassifier syncFailureClassifier;
    private final RateLimitService rateLimitService;

    SyncRecoveryExecutor(
        RepositoryFacade repositoryFacade,
        IssueFacade issueFacade,
        CommentFacade commentFacade,
        PlatformConnectionFacade platformConnectionFacade,
        PlatformGatewayResolver platformGatewayResolver,
        SyncStateService syncStateService,
        SyncRunService syncRunService,
        SyncFailureService syncFailureService,
        SyncFailureClassifier syncFailureClassifier,
        RateLimitService rateLimitService
    ) {
        this.repositoryFacade = repositoryFacade;
        this.issueFacade = issueFacade;
        this.commentFacade = commentFacade;
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformGatewayResolver = platformGatewayResolver;
        this.syncStateService = syncStateService;
        this.syncRunService = syncRunService;
        this.syncFailureService = syncFailureService;
        this.syncFailureClassifier = syncFailureClassifier;
        this.rateLimitService = rateLimitService;
    }

    SyncRunResponse refreshRepositories(PlatformType platform, HttpSession session, String triggerType, String operation) {
        CurrentConnection connection = platformConnectionFacade.requireCurrentConnection(platform, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        String resourceKey = platform.name() + ":" + connection.accountLogin();
        SyncRun syncRun = syncRunService.start(platform, SyncResourceType.REPOSITORY_LIST, resourceKey, triggerType);
        try {
            var result = platformGatewayResolver.getGateway(platform)
                .getAccessibleRepositoriesWithRateLimit(tokenAccess.accessToken(), tokenAccess.baseUrl());
            rateLimitService.record(result.rateLimitSnapshot());
            int updatedCount = repositoryFacade.upsertRepositories(platform, connection.accountLogin(), result.data()).size();
            syncRunService.completeSuccess(syncRun, updatedCount);
            syncStateService.recordSuccess(SyncResourceType.REPOSITORY_LIST, resourceKey, "Repository cache refreshed.");
            return syncRunService.toResponse(syncRun);
        } catch (RuntimeException exception) {
            throw recordFailure(syncRun, operation, exception);
        }
    }

    SyncRunResponse refreshRepositoryIssues(PlatformType platform, String repositoryId, HttpSession session, String triggerType, String operation) {
        RepositoryAccess repository = requireRepository(platform, repositoryId, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        String resourceKey = platform.name() + ":" + repository.externalId();
        SyncRun syncRun = syncRunService.start(platform, SyncResourceType.REPOSITORY, resourceKey, triggerType);
        try {
            var result = platformGatewayResolver.getGateway(platform)
                .getRepositoryIssuesWithRateLimit(tokenAccess.accessToken(), tokenAccess.baseUrl(), repository.ownerKey(), repository.name());
            rateLimitService.record(result.rateLimitSnapshot());
            int updatedCount = issueFacade.upsertIssues(platform, repository.externalId(), result.data()).size();
            syncRunService.completeSuccess(syncRun, updatedCount);
            syncStateService.recordSuccess(SyncResourceType.REPOSITORY, resourceKey, "Issue cache refreshed for repository.");
            return syncRunService.toResponse(syncRun);
        } catch (RuntimeException exception) {
            throw recordFailure(syncRun, operation, exception);
        }
    }

    SyncRunResponse refreshIssue(PlatformType platform, String repositoryId, String issueNumberOrKey, boolean includeComments, HttpSession session) {
        RepositoryAccess repository = requireRepository(platform, repositoryId, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        String resourceKey = platform.name() + ":" + repository.externalId() + ":" + issueNumberOrKey;
        SyncRun syncRun = syncRunService.start(platform, SyncResourceType.ISSUE, resourceKey, "MANUAL_RESYNC");
        try {
            var result = platformGatewayResolver.getGateway(platform)
                .getRepositoryIssuesWithRateLimit(tokenAccess.accessToken(), tokenAccess.baseUrl(), repository.ownerKey(), repository.name());
            rateLimitService.record(result.rateLimitSnapshot());
            RemoteIssue target = findIssue(result.data(), issueNumberOrKey);
            var issue = issueFacade.upsertIssue(platform, repository.externalId(), target);
            int updatedCount = 1;
            if (includeComments) {
                var comments = platformGatewayResolver.getGateway(platform)
                    .getIssueComments(tokenAccess.accessToken(), tokenAccess.baseUrl(), repository.ownerKey(), repository.name(), issue.numberOrKey());
                commentFacade.upsertComments(platform, issue.issueId(), comments);
                updatedCount += comments.size();
            }
            syncRunService.completeSuccess(syncRun, updatedCount);
            syncStateService.recordSuccess(SyncResourceType.ISSUE, resourceKey, "Issue cache refreshed.");
            return syncRunService.toResponse(syncRun);
        } catch (RuntimeException exception) {
            throw recordFailure(syncRun, "RESYNC_ISSUE", exception);
        }
    }

    private RepositoryAccess requireRepository(PlatformType platform, String repositoryId, HttpSession session) {
        CurrentConnection connection = platformConnectionFacade.requireCurrentConnection(platform, session);
        return repositoryFacade.requireAccessibleRepository(platform, repositoryId, connection.accountLogin());
    }

    private RemoteIssue findIssue(List<RemoteIssue> issues, String issueNumberOrKey) {
        return issues.stream()
            .filter(issue -> issueNumberOrKey.equals(issue.numberOrKey()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Remote issue not found: " + issueNumberOrKey));
    }

    private SyncOperationFailedException recordFailure(SyncRun syncRun, String operation, RuntimeException exception) {
        var failureType = syncFailureClassifier.classify(exception);
        String message = syncFailureClassifier.message(exception);
        boolean retryable = syncFailureClassifier.isRetryable(failureType);
        var status = failureType.name().equals("RATE_LIMITED") ? SyncRunStatus.RATE_LIMITED : SyncRunStatus.FAILED;
        if (status == SyncRunStatus.RATE_LIMITED) {
            syncRunService.completeRateLimited(syncRun, message);
        } else {
            syncRunService.completeFailed(syncRun, message);
        }
        SyncFailure failure = syncFailureService.recordFailure(
            syncRun,
            operation,
            failureType,
            retryable,
            syncFailureClassifier.nextRetryAt(exception),
            message
        );
        syncStateService.recordFailure(syncRun.getResourceType(), syncRun.getResourceKey(), message);
        return new SyncOperationFailedException(
            message,
            syncRun.getId(),
            failure.getId(),
            status.name(),
            retryable,
            failure.getNextRetryAt(),
            exception
        );
    }
}
