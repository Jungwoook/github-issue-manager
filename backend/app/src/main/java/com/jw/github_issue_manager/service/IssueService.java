package com.jw.github_issue_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.TokenAccess;
import com.jw.github_issue_manager.domain.IssueCache;
import com.jw.github_issue_manager.domain.RepositoryCache;
import com.jw.github_issue_manager.domain.SyncResourceType;
import com.jw.github_issue_manager.dto.issue.CreateIssueRequest;
import com.jw.github_issue_manager.dto.issue.IssueDetailResponse;
import com.jw.github_issue_manager.dto.issue.IssueSummaryResponse;
import com.jw.github_issue_manager.dto.issue.UpdateIssueRequest;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.exception.ResourceNotFoundException;
import com.jw.github_issue_manager.repository.IssueCacheRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class IssueService {

    private final IssueCacheRepository issueCacheRepository;
    private final RepositoryService repositoryService;
    private final SyncStateService syncStateService;
    private final PlatformConnectionFacade platformConnectionFacade;
    private final PlatformGatewayResolver platformGatewayResolver;

    public IssueService(
        IssueCacheRepository issueCacheRepository,
        RepositoryService repositoryService,
        SyncStateService syncStateService,
        PlatformConnectionFacade platformConnectionFacade,
        PlatformGatewayResolver platformGatewayResolver
    ) {
        this.issueCacheRepository = issueCacheRepository;
        this.repositoryService = repositoryService;
        this.syncStateService = syncStateService;
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformGatewayResolver = platformGatewayResolver;
    }

    @Transactional(readOnly = true)
    public List<IssueSummaryResponse> getIssues(
        PlatformType platform,
        String repositoryId,
        String keyword,
        String state,
        HttpSession session
    ) {
        repositoryService.requireAccessibleRepository(platform, repositoryId, session);

        return issueCacheRepository.findByPlatformAndRepositoryExternalIdOrderByNumberOrKeyDesc(platform, repositoryId).stream()
            .filter(issue -> keyword == null || keyword.isBlank() || containsIgnoreCase(issue.getTitle(), keyword) || containsIgnoreCase(issue.getBody(), keyword))
            .filter(issue -> state == null || state.isBlank() || issue.getState().equalsIgnoreCase(state))
            .map(this::toSummaryResponse)
            .toList();
    }

    @Transactional
    public List<IssueSummaryResponse> refreshIssues(PlatformType platform, String repositoryId, HttpSession session) {
        RepositoryCache repository = repositoryService.requireAccessibleRepository(platform, repositoryId, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        List<RemoteIssue> issues = platformGatewayResolver.getGateway(platform).getRepositoryIssues(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.getOwnerKey(),
            repository.getName()
        );
        issues.forEach(issue -> upsertIssue(repository, issue));

        syncStateService.recordSuccess(
            SyncResourceType.REPOSITORY,
            repositoryKey(platform, repositoryId),
            "Issue cache refreshed for repository."
        );

        return getIssues(platform, repositoryId, null, null, session);
    }

    @Transactional
    public IssueDetailResponse createIssue(PlatformType platform, String repositoryId, CreateIssueRequest request, HttpSession session) {
        RepositoryCache repository = repositoryService.requireAccessibleRepository(platform, repositoryId, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        RemoteIssue createdIssue = platformGatewayResolver.getGateway(platform).createIssue(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.getOwnerKey(),
            repository.getName(),
            request.title(),
            request.body()
        );
        IssueCache issue = upsertIssue(repository, createdIssue);

        syncStateService.recordSuccess(
            SyncResourceType.ISSUE,
            issueKey(platform, repositoryId, issue.getNumberOrKey()),
            "Issue created in cache."
        );

        return toDetailResponse(issue);
    }

    @Transactional(readOnly = true)
    public IssueDetailResponse getIssue(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        return toDetailResponse(requireIssue(platform, repositoryId, issueNumberOrKey, session));
    }

    @Transactional
    public IssueDetailResponse updateIssue(
        PlatformType platform,
        String repositoryId,
        String issueNumberOrKey,
        UpdateIssueRequest request,
        HttpSession session
    ) {
        RepositoryCache repository = repositoryService.requireAccessibleRepository(platform, repositoryId, session);
        IssueCache currentIssue = requireIssue(platform, repositoryId, issueNumberOrKey, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);

        RemoteIssue updatedIssue = platformGatewayResolver.getGateway(platform).updateIssue(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.getOwnerKey(),
            repository.getName(),
            issueNumberOrKey,
            request.title() != null ? request.title() : currentIssue.getTitle(),
            request.body() != null ? request.body() : currentIssue.getBody(),
            normalizeState(request.state(), currentIssue.getState())
        );
        IssueCache issue = upsertIssue(repository, updatedIssue);

        syncStateService.recordSuccess(
            SyncResourceType.ISSUE,
            issueKey(platform, repositoryId, issueNumberOrKey),
            "Issue cache updated."
        );

        return toDetailResponse(issue);
    }

    @Transactional
    public void deleteIssue(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        RepositoryCache repository = repositoryService.requireAccessibleRepository(platform, repositoryId, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        platformGatewayResolver.getGateway(platform).updateIssue(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.getOwnerKey(),
            repository.getName(),
            issueNumberOrKey,
            null,
            null,
            "CLOSED"
        );
        refreshIssues(platform, repositoryId, session);
        syncStateService.recordSuccess(
            SyncResourceType.ISSUE,
            issueKey(platform, repositoryId, issueNumberOrKey),
            "Issue was closed on platform."
        );
    }

    @Transactional(readOnly = true)
    public SyncStateResponse getIssueSyncState(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        requireIssue(platform, repositoryId, issueNumberOrKey, session);
        return syncStateService.getSyncState(SyncResourceType.ISSUE, issueKey(platform, repositoryId, issueNumberOrKey));
    }

    @Transactional(readOnly = true)
    public IssueCache requireIssue(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        repositoryService.requireAccessibleRepository(platform, repositoryId, session);
        return issueCacheRepository.findByPlatformAndRepositoryExternalIdAndNumberOrKey(platform, repositoryId, issueNumberOrKey)
            .orElseThrow(() -> new ResourceNotFoundException("ISSUE_NOT_FOUND", "Issue was not found."));
    }

    private IssueCache upsertIssue(RepositoryCache repository, RemoteIssue issueInfo) {
        return issueCacheRepository.findByPlatformAndRepositoryExternalIdAndNumberOrKey(
                repository.getPlatform(),
                repository.getExternalId(),
                issueInfo.numberOrKey()
            )
            .map(existing -> {
                existing.update(issueInfo.title(), issueInfo.body(), normalizeState(issueInfo.state(), existing.getState()), issueInfo.updatedAt());
                return existing;
            })
            .orElseGet(() -> issueCacheRepository.save(new IssueCache(
                repository.getPlatform(),
                issueInfo.externalId(),
                repository.getExternalId(),
                issueInfo.numberOrKey(),
                issueInfo.title(),
                issueInfo.body(),
                normalizeState(issueInfo.state(), "OPEN"),
                issueInfo.authorLogin(),
                issueInfo.createdAt(),
                issueInfo.updatedAt(),
                issueInfo.closedAt(),
                issueInfo.updatedAt()
            )));
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && source.toLowerCase().contains(target.toLowerCase());
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

    private IssueSummaryResponse toSummaryResponse(IssueCache issue) {
        return new IssueSummaryResponse(
            issue.getPlatform(),
            issue.getExternalId(),
            issue.getNumberOrKey(),
            issue.getTitle(),
            issue.getState(),
            issue.getAuthorLogin(),
            issue.getCreatedAt(),
            issue.getUpdatedAt(),
            issue.getLastSyncedAt()
        );
    }

    private IssueDetailResponse toDetailResponse(IssueCache issue) {
        return new IssueDetailResponse(
            issue.getPlatform(),
            issue.getExternalId(),
            issue.getRepositoryExternalId(),
            issue.getNumberOrKey(),
            issue.getTitle(),
            issue.getBody(),
            issue.getState(),
            issue.getAuthorLogin(),
            issue.getCreatedAt(),
            issue.getUpdatedAt(),
            issue.getClosedAt(),
            issue.getLastSyncedAt()
        );
    }
}
