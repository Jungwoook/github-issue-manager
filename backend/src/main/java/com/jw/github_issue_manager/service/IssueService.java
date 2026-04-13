package com.jw.github_issue_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
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
    private final AuthService authService;
    private final PlatformGatewayResolver platformGatewayResolver;

    public IssueService(
        IssueCacheRepository issueCacheRepository,
        RepositoryService repositoryService,
        SyncStateService syncStateService,
        AuthService authService,
        PlatformGatewayResolver platformGatewayResolver
    ) {
        this.issueCacheRepository = issueCacheRepository;
        this.repositoryService = repositoryService;
        this.syncStateService = syncStateService;
        this.authService = authService;
        this.platformGatewayResolver = platformGatewayResolver;
    }

    @Transactional(readOnly = true)
    public List<IssueSummaryResponse> getIssues(Long githubRepositoryId, String keyword, String state, HttpSession session) {
        repositoryService.requireAccessibleRepository(githubRepositoryId, session);

        return issueCacheRepository.findByPlatformAndRepositoryExternalIdOrderByNumberOrKeyDesc(
                PlatformType.GITHUB,
                githubRepositoryId.toString()
            ).stream()
            .filter(issue -> keyword == null || keyword.isBlank() || containsIgnoreCase(issue.getTitle(), keyword) || containsIgnoreCase(issue.getBody(), keyword))
            .filter(issue -> state == null || state.isBlank() || issue.getState().equalsIgnoreCase(state))
            .map(this::toSummaryResponse)
            .toList();
    }

    @Transactional
    public List<IssueSummaryResponse> refreshIssues(Long githubRepositoryId, HttpSession session) {
        RepositoryCache repository = repositoryService.requireAccessibleRepository(githubRepositoryId, session);
        String personalAccessToken = authService.requirePersonalAccessToken(session);
        List<RemoteIssue> issues = platformGatewayResolver.getGateway(PlatformType.GITHUB).getRepositoryIssues(
            personalAccessToken,
            repository.getOwnerKey(),
            repository.getName()
        );
        issues.forEach(issue -> upsertIssue(repository, issue));

        syncStateService.recordSuccess(
            SyncResourceType.REPOSITORY,
            githubRepositoryId.toString(),
            "Issue cache refreshed for repository."
        );

        return getIssues(githubRepositoryId, null, null, session);
    }

    @Transactional
    public IssueDetailResponse createIssue(Long githubRepositoryId, CreateIssueRequest request, HttpSession session) {
        RepositoryCache repository = repositoryService.requireAccessibleRepository(githubRepositoryId, session);
        String personalAccessToken = authService.requirePersonalAccessToken(session);
        RemoteIssue createdIssue = platformGatewayResolver.getGateway(PlatformType.GITHUB).createIssue(
            personalAccessToken,
            repository.getOwnerKey(),
            repository.getName(),
            request.title(),
            request.body()
        );
        IssueCache issue = upsertIssue(repository, createdIssue);

        syncStateService.recordSuccess(
            SyncResourceType.ISSUE,
            issueKey(githubRepositoryId, issue.getNumberOrKey()),
            "Issue created in cache."
        );

        return toDetailResponse(issue);
    }

    @Transactional(readOnly = true)
    public IssueDetailResponse getIssue(Long githubRepositoryId, Integer issueNumber, HttpSession session) {
        return toDetailResponse(requireIssue(githubRepositoryId, issueNumber, session));
    }

    @Transactional
    public IssueDetailResponse updateIssue(Long githubRepositoryId, Integer issueNumber, UpdateIssueRequest request, HttpSession session) {
        RepositoryCache repository = repositoryService.requireAccessibleRepository(githubRepositoryId, session);
        IssueCache currentIssue = requireIssue(githubRepositoryId, issueNumber, session);
        String personalAccessToken = authService.requirePersonalAccessToken(session);

        RemoteIssue updatedIssue = platformGatewayResolver.getGateway(PlatformType.GITHUB).updateIssue(
            personalAccessToken,
            repository.getOwnerKey(),
            repository.getName(),
            issueNumber.toString(),
            request.title() != null ? request.title() : currentIssue.getTitle(),
            request.body() != null ? request.body() : currentIssue.getBody(),
            normalizeState(request.state(), currentIssue.getState())
        );
        IssueCache issue = upsertIssue(repository, updatedIssue);

        syncStateService.recordSuccess(
            SyncResourceType.ISSUE,
            issueKey(githubRepositoryId, issueNumber.toString()),
            "Issue cache updated."
        );

        return toDetailResponse(issue);
    }

    @Transactional
    public void deleteIssue(Long githubRepositoryId, Integer issueNumber, HttpSession session) {
        RepositoryCache repository = repositoryService.requireAccessibleRepository(githubRepositoryId, session);
        String personalAccessToken = authService.requirePersonalAccessToken(session);
        platformGatewayResolver.getGateway(PlatformType.GITHUB).updateIssue(
            personalAccessToken,
            repository.getOwnerKey(),
            repository.getName(),
            issueNumber.toString(),
            null,
            null,
            "CLOSED"
        );
        refreshIssues(githubRepositoryId, session);
        syncStateService.recordSuccess(
            SyncResourceType.ISSUE,
            issueKey(githubRepositoryId, issueNumber.toString()),
            "Issue was closed on GitHub."
        );
    }

    @Transactional(readOnly = true)
    public SyncStateResponse getIssueSyncState(Long githubRepositoryId, Integer issueNumber, HttpSession session) {
        requireIssue(githubRepositoryId, issueNumber, session);
        return syncStateService.getSyncState(SyncResourceType.ISSUE, issueKey(githubRepositoryId, issueNumber.toString()));
    }

    @Transactional(readOnly = true)
    public IssueCache requireIssue(Long githubRepositoryId, Integer issueNumber, HttpSession session) {
        repositoryService.requireAccessibleRepository(githubRepositoryId, session);
        return issueCacheRepository.findByPlatformAndRepositoryExternalIdAndNumberOrKey(
                PlatformType.GITHUB,
                githubRepositoryId.toString(),
                issueNumber.toString()
            )
            .orElseThrow(() -> new ResourceNotFoundException("ISSUE_NOT_FOUND", "Issue was not found."));
    }

    private IssueCache upsertIssue(RepositoryCache repository, RemoteIssue issueInfo) {
        return issueCacheRepository.findByPlatformAndRepositoryExternalIdAndNumberOrKey(
                PlatformType.GITHUB,
                repository.getExternalId(),
                issueInfo.numberOrKey()
            )
            .map(existing -> {
                existing.update(issueInfo.title(), issueInfo.body(), normalizeState(issueInfo.state(), existing.getState()), issueInfo.updatedAt());
                return existing;
            })
            .orElseGet(() -> issueCacheRepository.save(new IssueCache(
                PlatformType.GITHUB,
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

    private String issueKey(Long githubRepositoryId, String issueNumberOrKey) {
        return githubRepositoryId + ":" + issueNumberOrKey;
    }

    private IssueSummaryResponse toSummaryResponse(IssueCache issue) {
        return new IssueSummaryResponse(
            Long.parseLong(issue.getExternalId()),
            Integer.parseInt(issue.getNumberOrKey()),
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
            Long.parseLong(issue.getExternalId()),
            Long.parseLong(issue.getRepositoryExternalId()),
            Integer.parseInt(issue.getNumberOrKey()),
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
