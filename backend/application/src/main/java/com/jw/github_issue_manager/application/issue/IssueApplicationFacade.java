package com.jw.github_issue_manager.application.issue;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.sync.SyncResourceType;
import com.jw.github_issue_manager.application.sync.SyncStateResponse;
import com.jw.github_issue_manager.application.sync.SyncStateService;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.TokenAccess;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.issue.api.IssueFacade;
import com.jw.github_issue_manager.issue.api.IssueAccess;
import com.jw.github_issue_manager.issue.api.dto.CreateIssueRequest;
import com.jw.github_issue_manager.issue.api.dto.IssueDetailResponse;
import com.jw.github_issue_manager.issue.api.dto.IssueSummaryResponse;
import com.jw.github_issue_manager.issue.api.dto.UpdateIssueRequest;
import com.jw.github_issue_manager.repository.api.RepositoryAccess;
import com.jw.github_issue_manager.repository.api.RepositoryFacade;

import jakarta.servlet.http.HttpSession;

@Service
public class IssueApplicationFacade {

    private final IssueFacade issueFacade;
    private final RepositoryFacade repositoryFacade;
    private final PlatformConnectionFacade platformConnectionFacade;
    private final PlatformGatewayResolver platformGatewayResolver;
    private final SyncStateService syncStateService;

    public IssueApplicationFacade(
        IssueFacade issueFacade,
        RepositoryFacade repositoryFacade,
        PlatformConnectionFacade platformConnectionFacade,
        PlatformGatewayResolver platformGatewayResolver,
        SyncStateService syncStateService
    ) {
        this.issueFacade = issueFacade;
        this.repositoryFacade = repositoryFacade;
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformGatewayResolver = platformGatewayResolver;
        this.syncStateService = syncStateService;
    }

    public List<IssueSummaryResponse> getIssues(
        String platform,
        String repositoryId,
        String keyword,
        String state,
        HttpSession session
    ) {
        PlatformType platformType = PlatformType.from(platform);
        requireRepository(platformType, repositoryId, session);
        return issueFacade.getIssues(platformType, repositoryId, keyword, state);
    }

    public List<IssueSummaryResponse> refreshIssues(String platform, String repositoryId, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        RepositoryAccess repository = requireRepository(platformType, repositoryId, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platformType, session);
        var issues = platformGatewayResolver.getGateway(platformType)
            .getRepositoryIssues(tokenAccess.accessToken(), tokenAccess.baseUrl(), repository.ownerKey(), repository.name());
        List<IssueSummaryResponse> responses = issueFacade.upsertIssues(platformType, repository.externalId(), issues);

        syncStateService.recordSuccess(
            SyncResourceType.REPOSITORY,
            repositoryKey(platformType, repository.externalId()),
            "Issue cache refreshed for repository."
        );

        return responses;
    }

    public IssueDetailResponse createIssue(
        String platform,
        String repositoryId,
        CreateIssueRequest request,
        HttpSession session
    ) {
        PlatformType platformType = PlatformType.from(platform);
        RepositoryAccess repository = requireRepository(platformType, repositoryId, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platformType, session);
        var createdIssue = platformGatewayResolver.getGateway(platformType)
            .createIssue(tokenAccess.accessToken(), tokenAccess.baseUrl(), repository.ownerKey(), repository.name(), request.title(), request.body());
        IssueDetailResponse response = issueFacade.upsertIssue(platformType, repository.externalId(), createdIssue);

        syncStateService.recordSuccess(
            SyncResourceType.ISSUE,
            issueKey(platformType, repository.externalId(), response.numberOrKey()),
            "Issue created in cache."
        );

        return response;
    }

    public IssueDetailResponse getIssue(String platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        requireRepository(platformType, repositoryId, session);
        return issueFacade.getIssue(platformType, repositoryId, issueNumberOrKey);
    }

    public IssueDetailResponse updateIssue(
        String platform,
        String repositoryId,
        String issueNumberOrKey,
        UpdateIssueRequest request,
        HttpSession session
    ) {
        PlatformType platformType = PlatformType.from(platform);
        RepositoryAccess repository = requireRepository(platformType, repositoryId, session);
        IssueDetailResponse currentIssue = issueFacade.getIssue(platformType, repository.externalId(), issueNumberOrKey);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platformType, session);
        var updatedIssue = platformGatewayResolver.getGateway(platformType).updateIssue(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.ownerKey(),
            repository.name(),
            issueNumberOrKey,
            request.title() != null ? request.title() : currentIssue.title(),
            request.body() != null ? request.body() : currentIssue.body(),
            normalizeState(request.state(), currentIssue.state())
        );
        IssueDetailResponse response = issueFacade.upsertIssue(platformType, repository.externalId(), updatedIssue);

        syncStateService.recordSuccess(
            SyncResourceType.ISSUE,
            issueKey(platformType, repository.externalId(), issueNumberOrKey),
            "Issue cache updated."
        );

        return response;
    }

    public void deleteIssue(String platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        RepositoryAccess repository = requireRepository(platformType, repositoryId, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platformType, session);
        platformGatewayResolver.getGateway(platformType).updateIssue(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.ownerKey(),
            repository.name(),
            issueNumberOrKey,
            null,
            null,
            "CLOSED"
        );
        refreshIssues(platform, repositoryId, session);
        syncStateService.recordSuccess(
            SyncResourceType.ISSUE,
            issueKey(platformType, repository.externalId(), issueNumberOrKey),
            "Issue was closed on platform."
        );
    }

    public SyncStateResponse getIssueSyncState(String platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        requireIssue(platformType, repositoryId, issueNumberOrKey, session);
        return syncStateService.getSyncState(SyncResourceType.ISSUE, issueKey(platformType, repositoryId, issueNumberOrKey));
    }

    public IssueAccess requireIssue(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        requireRepository(platform, repositoryId, session);
        return issueFacade.requireIssue(platform, repositoryId, issueNumberOrKey);
    }

    private RepositoryAccess requireRepository(PlatformType platform, String repositoryId, HttpSession session) {
        var connection = platformConnectionFacade.requireCurrentConnection(platform, session);
        return repositoryFacade.requireAccessibleRepository(platform, repositoryId, connection.accountLogin());
    }

    private String normalizeState(String state, String currentState) {
        if (state == null || state.isBlank()) {
            return currentState;
        }
        return state.toUpperCase();
    }

    private String repositoryKey(PlatformType platform, String repositoryId) {
        return platform.name() + ":" + repositoryId;
    }

    private String issueKey(PlatformType platform, String repositoryId, String issueNumberOrKey) {
        return platform.name() + ":" + repositoryId + ":" + issueNumberOrKey;
    }
}
