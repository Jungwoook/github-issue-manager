package com.jw.github_issue_manager.application.issue;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.issue.api.IssueFacade;
import com.jw.github_issue_manager.issue.api.dto.CreateIssueRequest;
import com.jw.github_issue_manager.issue.api.dto.IssueDetailResponse;
import com.jw.github_issue_manager.issue.api.dto.IssueSummaryResponse;
import com.jw.github_issue_manager.issue.api.dto.UpdateIssueRequest;
import com.jw.github_issue_manager.shared.api.dto.SyncStateResponse;

import jakarta.servlet.http.HttpSession;

@Service
public class IssueApplicationFacade {

    private final IssueFacade issueFacade;

    public IssueApplicationFacade(IssueFacade issueFacade) {
        this.issueFacade = issueFacade;
    }

    public List<IssueSummaryResponse> getIssues(
        String platform,
        String repositoryId,
        String keyword,
        String state,
        HttpSession session
    ) {
        return issueFacade.getIssues(PlatformType.from(platform), repositoryId, keyword, state, session);
    }

    public List<IssueSummaryResponse> refreshIssues(String platform, String repositoryId, HttpSession session) {
        return issueFacade.refreshIssues(PlatformType.from(platform), repositoryId, session);
    }

    public IssueDetailResponse createIssue(
        String platform,
        String repositoryId,
        CreateIssueRequest request,
        HttpSession session
    ) {
        return issueFacade.createIssue(PlatformType.from(platform), repositoryId, request, session);
    }

    public IssueDetailResponse getIssue(String platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        return issueFacade.getIssue(PlatformType.from(platform), repositoryId, issueNumberOrKey, session);
    }

    public IssueDetailResponse updateIssue(
        String platform,
        String repositoryId,
        String issueNumberOrKey,
        UpdateIssueRequest request,
        HttpSession session
    ) {
        return issueFacade.updateIssue(PlatformType.from(platform), repositoryId, issueNumberOrKey, request, session);
    }

    public void deleteIssue(String platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        issueFacade.deleteIssue(PlatformType.from(platform), repositoryId, issueNumberOrKey, session);
    }

    public SyncStateResponse getIssueSyncState(String platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        return issueFacade.getIssueSyncState(PlatformType.from(platform), repositoryId, issueNumberOrKey, session);
    }
}
