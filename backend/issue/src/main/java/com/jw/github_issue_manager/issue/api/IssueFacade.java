package com.jw.github_issue_manager.issue.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.issue.api.dto.IssueDetailResponse;
import com.jw.github_issue_manager.issue.api.dto.IssueSummaryResponse;
import com.jw.github_issue_manager.issue.internal.service.IssueService;

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
        String state
    ) {
        return issueService.getIssues(platform, repositoryId, keyword, state);
    }

    public List<IssueSummaryResponse> upsertIssues(PlatformType platform, String repositoryId, List<RemoteIssue> issues) {
        return issueService.upsertIssues(platform, repositoryId, issues);
    }

    public IssueDetailResponse upsertIssue(PlatformType platform, String repositoryId, RemoteIssue issue) {
        return issueService.upsertIssue(platform, repositoryId, issue);
    }

    public IssueDetailResponse getIssue(PlatformType platform, String repositoryId, String issueNumberOrKey) {
        return issueService.getIssue(platform, repositoryId, issueNumberOrKey);
    }

    public IssueAccess requireIssue(PlatformType platform, String repositoryId, String issueNumberOrKey) {
        return issueService.requireIssue(platform, repositoryId, issueNumberOrKey);
    }
}
