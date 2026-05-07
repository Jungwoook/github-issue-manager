package com.jw.github_issue_manager.application.repository;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.sync.SyncResourceType;
import com.jw.github_issue_manager.application.sync.SyncStateResponse;
import com.jw.github_issue_manager.application.sync.SyncStateService;
import com.jw.github_issue_manager.application.sync.SyncFailureClassifier;
import com.jw.github_issue_manager.application.sync.failure.SyncFailureService;
import com.jw.github_issue_manager.application.sync.run.SyncRun;
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

    public RepositoryApplicationFacade(
        RepositoryFacade repositoryFacade,
        PlatformConnectionFacade platformConnectionFacade,
        PlatformGatewayResolver platformGatewayResolver,
        SyncStateService syncStateService,
        SyncRunService syncRunService,
        SyncFailureService syncFailureService,
        SyncFailureClassifier syncFailureClassifier
    ) {
        this.repositoryFacade = repositoryFacade;
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformGatewayResolver = platformGatewayResolver;
        this.syncStateService = syncStateService;
        this.syncRunService = syncRunService;
        this.syncFailureService = syncFailureService;
        this.syncFailureClassifier = syncFailureClassifier;
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
            var repositories = platformGatewayResolver.getGateway(platformType)
                .getAccessibleRepositories(tokenAccess.accessToken(), tokenAccess.baseUrl());
            List<RepositoryResponse> responses = repositoryFacade.upsertRepositories(platformType, connection.accountLogin(), repositories);

            syncRunService.completeSuccess(syncRun, responses.size());
            syncStateService.recordSuccess(
                SyncResourceType.REPOSITORY_LIST,
                resourceKey,
                "Repository cache refreshed."
            );

            return responses;
        } catch (RuntimeException exception) {
            recordFailure(syncRun, "REFRESH_REPOSITORIES", exception);
            throw exception;
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

    private void recordFailure(SyncRun syncRun, String operation, RuntimeException exception) {
        var failureType = syncFailureClassifier.classify(exception);
        String message = syncFailureClassifier.message(exception);
        syncRunService.completeFailed(syncRun, message);
        syncFailureService.recordFailure(
            syncRun,
            operation,
            failureType,
            syncFailureClassifier.isRetryable(failureType),
            null,
            message
        );
        syncStateService.recordFailure(syncRun.getResourceType(), syncRun.getResourceKey(), message);
    }
}
