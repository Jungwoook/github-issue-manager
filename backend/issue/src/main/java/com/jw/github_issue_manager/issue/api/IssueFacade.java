package com.jw.github_issue_manager.issue.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.dto.issue.CreateIssueRequest;
import com.jw.github_issue_manager.dto.issue.IssueDetailResponse;
import com.jw.github_issue_manager.dto.issue.IssueSummaryResponse;
import com.jw.github_issue_manager.dto.issue.UpdateIssueRequest;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.issue.internal.service.IssueService;

import jakarta.servlet.http.HttpSession;

@Service
public class IssueFacade {

    private final IssueService issueService;

    public IssueFacade(IssueService issueService) {
        this.issueService = issueService;
    }

    public List<IssueSummaryResponse> getIssues(
        PlatformType platform,
        String repositoryId,
        String keyword,
        String state,
        HttpSession session
    ) {
        return issueService.getIssues(platform, repositoryId, keyword, state, session);
    }

    public List<IssueSummaryResponse> refreshIssues(PlatformType platform, String repositoryId, HttpSession session) {
        return issueService.refreshIssues(platform, repositoryId, session);
    }

    public IssueDetailResponse createIssue(
        PlatformType platform,
        String repositoryId,
        CreateIssueRequest request,
        HttpSession session
    ) {
        return issueService.createIssue(platform, repositoryId, request, session);
    }

    public IssueDetailResponse getIssue(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        return issueService.getIssue(platform, repositoryId, issueNumberOrKey, session);
    }

    public IssueDetailResponse updateIssue(
        PlatformType platform,
        String repositoryId,
        String issueNumberOrKey,
        UpdateIssueRequest request,
        HttpSession session
    ) {
        return issueService.updateIssue(platform, repositoryId, issueNumberOrKey, request, session);
    }

    public void deleteIssue(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        issueService.deleteIssue(platform, repositoryId, issueNumberOrKey, session);
    }

    public SyncStateResponse getIssueSyncState(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        return issueService.getIssueSyncState(platform, repositoryId, issueNumberOrKey, session);
    }

    public IssueAccess requireIssue(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        return issueService.requireIssue(platform, repositoryId, issueNumberOrKey, session);
    }
}
