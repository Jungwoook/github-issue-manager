package com.jw.github_issue_manager.application.repository;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.ratelimit.RateLimitService;
import com.jw.github_issue_manager.application.sync.SyncResourceType;
import com.jw.github_issue_manager.application.sync.SyncOperationFailedException;
import com.jw.github_issue_manager.application.sync.SyncStateResponse;
import com.jw.github_issue_manager.application.sync.SyncStateService;
import com.jw.github_issue_manager.application.sync.SyncFailureClassifier;
import com.jw.github_issue_manager.application.sync.failure.SyncFailure;
import com.jw.github_issue_manager.application.sync.failure.SyncFailureService;
import com.jw.github_issue_manager.application.sync.run.SyncRun;
import com.jw.github_issue_manager.application.sync.run.SyncRunStatus;
import com.jw.github_issue_manager.application.sync.run.SyncRunService;
import com.jw.github_issue_manager.connection.api.CurrentConnection;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.TokenAccess;
import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.repository.api.RepositoryFacade;
import com.jw.github_issue_manager.repository.api.dto.RepositoryResponse;

import jakarta.servlet.http.HttpSession;

@Service
public class RepositoryApplicationFacade {

    private final RepositoryFacade repositoryFacade;
    private final PlatformConnectionFacade platformConnectionFacade;
    private final PlatformGatewayResolver platformGatewayResolver;
    private final SyncStateService syncStateService;
    private final SyncRunService syncRunService;
    private final SyncFailureService syncFailureService;
    private final SyncFailureClassifier syncFailureClassifier;
    private final RateLimitService rateLimitService;

    public RepositoryApplicationFacade(
        RepositoryFacade repositoryFacade,
        PlatformConnectionFacade platformConnectionFacade,
        PlatformGatewayResolver platformGatewayResolver,
        SyncStateService syncStateService,
        SyncRunService syncRunService,
        SyncFailureService syncFailureService,
        SyncFailureClassifier syncFailureClassifier,
        RateLimitService rateLimitService
    ) {
        this.repositoryFacade = repositoryFacade;
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformGatewayResolver = platformGatewayResolver;
        this.syncStateService = syncStateService;
        this.syncRunService = syncRunService;
        this.syncFailureService = syncFailureService;
        this.syncFailureClassifier = syncFailureClassifier;
        this.rateLimitService = rateLimitService;
    }

    public List<RepositoryResponse> getRepositories(String platform, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        CurrentConnection connection = platformConnectionFacade.requireCurrentConnection(platformType, session);
        return repositoryFacade.getRepositories(platformType, connection.accountLogin());
    }

    public List<RepositoryResponse> refreshRepositories(String platform, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        CurrentConnection connection = platformConnectionFacade.requireCurrentConnection(platformType, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platformType, session);
        String resourceKey = platformType.name() + ":" + connection.accountLogin();
        SyncRun syncRun = syncRunService.start(platformType, SyncResourceType.REPOSITORY_LIST, resourceKey, "MANUAL");
        try {
            var result = platformGatewayResolver.getGateway(platformType)
                .getAccessibleRepositoriesWithRateLimit(tokenAccess.accessToken(), tokenAccess.baseUrl());
            rateLimitService.record(result.rateLimitSnapshot());
            List<RepositoryResponse> responses = repositoryFacade.upsertRepositories(platformType, connection.accountLogin(), result.data());

            syncRunService.completeSuccess(syncRun, responses.size());
            syncStateService.recordSuccess(
                SyncResourceType.REPOSITORY_LIST,
                resourceKey,
                "Repository cache refreshed."
            );

            return responses;
        } catch (RuntimeException exception) {
            throw recordFailure(syncRun, "REFRESH_REPOSITORIES", exception);
        }
    }

    public RepositoryResponse getRepository(String platform, String repositoryId, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        CurrentConnection connection = platformConnectionFacade.requireCurrentConnection(platformType, session);
        return repositoryFacade.getRepository(platformType, repositoryId, connection.accountLogin());
    }

    public SyncStateResponse getRepositorySyncState(String platform, String repositoryId, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        CurrentConnection connection = platformConnectionFacade.requireCurrentConnection(platformType, session);
        repositoryFacade.requireAccessibleRepository(platformType, repositoryId, connection.accountLogin());
        return syncStateService.getSyncState(SyncResourceType.REPOSITORY, resourceKey(platformType, repositoryId));
    }

    private String resourceKey(PlatformType platform, String repositoryId) {
        return platform.name() + ":" + repositoryId;
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
