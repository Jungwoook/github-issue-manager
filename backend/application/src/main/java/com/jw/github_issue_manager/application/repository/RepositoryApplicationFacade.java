package com.jw.github_issue_manager.application.repository;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.sync.SyncResourceType;
import com.jw.github_issue_manager.application.sync.SyncStateResponse;
import com.jw.github_issue_manager.application.sync.SyncStateService;
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

    public RepositoryApplicationFacade(
        RepositoryFacade repositoryFacade,
        PlatformConnectionFacade platformConnectionFacade,
        PlatformGatewayResolver platformGatewayResolver,
        SyncStateService syncStateService
    ) {
        this.repositoryFacade = repositoryFacade;
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformGatewayResolver = platformGatewayResolver;
        this.syncStateService = syncStateService;
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
        var repositories = platformGatewayResolver.getGateway(platformType)
            .getAccessibleRepositories(tokenAccess.accessToken(), tokenAccess.baseUrl());
        List<RepositoryResponse> responses = repositoryFacade.upsertRepositories(platformType, connection.accountLogin(), repositories);

        syncStateService.recordSuccess(
            SyncResourceType.REPOSITORY_LIST,
            platformType.name() + ":" + connection.accountLogin(),
            "Repository cache refreshed."
        );

        return responses;
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
}
