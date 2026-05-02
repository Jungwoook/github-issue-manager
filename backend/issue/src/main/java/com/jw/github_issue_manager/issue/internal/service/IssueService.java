package com.jw.github_issue_manager.issue.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.issue.api.IssueAccess;
import com.jw.github_issue_manager.issue.api.IssueNotFoundException;
import com.jw.github_issue_manager.issue.api.dto.IssueDetailResponse;
import com.jw.github_issue_manager.issue.api.dto.IssueSummaryResponse;
import com.jw.github_issue_manager.issue.internal.domain.IssueCache;
import com.jw.github_issue_manager.issue.internal.repository.IssueCacheRepository;

@Service
public class IssueService {

    private final IssueCacheRepository issueCacheRepository;

    public IssueService(IssueCacheRepository issueCacheRepository) {
        this.issueCacheRepository = issueCacheRepository;
    }

    @Transactional(readOnly = true)
    public List<IssueSummaryResponse> getIssues(
        PlatformType platform,
        String repositoryId,
        String keyword,
        String state
    ) {
        return issueCacheRepository.findByPlatformAndRepositoryExternalIdOrderByNumberOrKeyDesc(platform, repositoryId).stream()
            .filter(issue -> keyword == null || keyword.isBlank() || containsIgnoreCase(issue.getTitle(), keyword) || containsIgnoreCase(issue.getBody(), keyword))
            .filter(issue -> state == null || state.isBlank() || issue.getState().equalsIgnoreCase(state))
            .map(this::toSummaryResponse)
            .toList();
    }

    @Transactional
    public List<IssueSummaryResponse> upsertIssues(PlatformType platform, String repositoryId, List<RemoteIssue> issues) {
        issues.forEach(issue -> upsertIssue(platform, repositoryId, issue));
        return getIssues(platform, repositoryId, null, null);
    }

    @Transactional
    public IssueDetailResponse upsertIssue(PlatformType platform, String repositoryId, RemoteIssue issue) {
        return toDetailResponse(upsertIssueCache(platform, repositoryId, issue));
    }

    @Transactional(readOnly = true)
    public IssueDetailResponse getIssue(PlatformType platform, String repositoryId, String issueNumberOrKey) {
        return toDetailResponse(requireIssueCache(platform, repositoryId, issueNumberOrKey));
    }

    @Transactional(readOnly = true)
    public IssueAccess requireIssue(PlatformType platform, String repositoryId, String issueNumberOrKey) {
        return toAccess(requireIssueCache(platform, repositoryId, issueNumberOrKey));
    }

    private IssueCache requireIssueCache(PlatformType platform, String repositoryId, String issueNumberOrKey) {
        return issueCacheRepository.findByPlatformAndRepositoryExternalIdAndNumberOrKey(platform, repositoryId, issueNumberOrKey)
            .orElseThrow(IssueNotFoundException::new);
    }

    private IssueCache upsertIssueCache(PlatformType platform, String repositoryId, RemoteIssue issueInfo) {
        return issueCacheRepository.findByPlatformAndRepositoryExternalIdAndNumberOrKey(
                platform,
                repositoryId,
                issueInfo.numberOrKey()
            )
            .map(existing -> {
                existing.update(issueInfo.title(), issueInfo.body(), normalizeState(issueInfo.state(), existing.getState()), issueInfo.updatedAt());
                return existing;
            })
            .orElseGet(() -> issueCacheRepository.save(new IssueCache(
                platform,
                issueInfo.externalId(),
                repositoryId,
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

    private IssueAccess toAccess(IssueCache issue) {
        return new IssueAccess(
            issue.getPlatform(),
            issue.getExternalId(),
            issue.getRepositoryExternalId(),
            issue.getNumberOrKey()
        );
    }
}
