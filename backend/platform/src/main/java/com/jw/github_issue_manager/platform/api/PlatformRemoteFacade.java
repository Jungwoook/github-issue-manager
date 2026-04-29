package com.jw.github_issue_manager.platform.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.connection.api.CurrentConnection;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.TokenAccess;
import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.platform.api.dto.CurrentPlatformConnection;

import jakarta.servlet.http.HttpSession;

@Service
public class PlatformRemoteFacade {

    private final PlatformConnectionFacade platformConnectionFacade;
    private final PlatformGatewayResolver platformGatewayResolver;

    public PlatformRemoteFacade(
        PlatformConnectionFacade platformConnectionFacade,
        PlatformGatewayResolver platformGatewayResolver
    ) {
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformGatewayResolver = platformGatewayResolver;
    }

    public CurrentPlatformConnection requireCurrentConnection(PlatformType platform, HttpSession session) {
        return toCurrentPlatformConnection(platformConnectionFacade.requireCurrentConnection(platform, session));
    }

    public List<RemoteRepository> getAccessibleRepositories(PlatformType platform, HttpSession session) {
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        return platformGatewayResolver.getGateway(platform)
            .getAccessibleRepositories(tokenAccess.accessToken(), tokenAccess.baseUrl());
    }

    public List<RemoteIssue> getRepositoryIssues(
        PlatformType platform,
        HttpSession session,
        String ownerKey,
        String repositoryName
    ) {
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        return platformGatewayResolver.getGateway(platform)
            .getRepositoryIssues(tokenAccess.accessToken(), tokenAccess.baseUrl(), ownerKey, repositoryName);
    }

    public RemoteIssue createIssue(
        PlatformType platform,
        HttpSession session,
        String ownerKey,
        String repositoryName,
        String title,
        String body
    ) {
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        return platformGatewayResolver.getGateway(platform)
            .createIssue(tokenAccess.accessToken(), tokenAccess.baseUrl(), ownerKey, repositoryName, title, body);
    }

    public RemoteIssue updateIssue(
        PlatformType platform,
        HttpSession session,
        String ownerKey,
        String repositoryName,
        String issueKey,
        String title,
        String body,
        String state
    ) {
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        return platformGatewayResolver.getGateway(platform)
            .updateIssue(tokenAccess.accessToken(), tokenAccess.baseUrl(), ownerKey, repositoryName, issueKey, title, body, state);
    }

    public List<RemoteComment> getIssueComments(
        PlatformType platform,
        HttpSession session,
        String ownerKey,
        String repositoryName,
        String issueKey
    ) {
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        return platformGatewayResolver.getGateway(platform)
            .getIssueComments(tokenAccess.accessToken(), tokenAccess.baseUrl(), ownerKey, repositoryName, issueKey);
    }

    public RemoteComment createComment(
        PlatformType platform,
        HttpSession session,
        String ownerKey,
        String repositoryName,
        String issueKey,
        String body
    ) {
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        return platformGatewayResolver.getGateway(platform)
            .createComment(tokenAccess.accessToken(), tokenAccess.baseUrl(), ownerKey, repositoryName, issueKey, body);
    }

    private CurrentPlatformConnection toCurrentPlatformConnection(CurrentConnection connection) {
        return new CurrentPlatformConnection(
            connection.platform(),
            connection.userId(),
            connection.externalUserId(),
            connection.accountLogin(),
            connection.avatarUrl(),
            connection.tokenScopes(),
            connection.baseUrl()
        );
    }
}
