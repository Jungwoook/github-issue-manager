package com.jw.github_issue_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.application.issue.IssueApplicationFacade;
import com.jw.github_issue_manager.issue.api.dto.CreateIssueRequest;
import com.jw.github_issue_manager.issue.api.dto.IssueDetailResponse;
import com.jw.github_issue_manager.issue.api.dto.IssueSummaryResponse;
import com.jw.github_issue_manager.issue.api.dto.UpdateIssueRequest;
import com.jw.github_issue_manager.shared.api.dto.SyncStateResponse;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/platforms/{platform}/repositories/{repositoryId}/issues")
public class IssueController {

    private final IssueApplicationFacade issueApplicationFacade;

    public IssueController(IssueApplicationFacade issueApplicationFacade) {
        this.issueApplicationFacade = issueApplicationFacade;
    }

    @GetMapping
    public ResponseEntity<List<IssueSummaryResponse>> getIssues(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String state,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueApplicationFacade.getIssues(platform, repositoryId, keyword, state, session));
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<IssueSummaryResponse>> refreshIssues(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueApplicationFacade.refreshIssues(platform, repositoryId, session));
    }

    @PostMapping
    public ResponseEntity<IssueDetailResponse> createIssue(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @Valid @RequestBody CreateIssueRequest request,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueApplicationFacade.createIssue(platform, repositoryId, request, session));
    }

    @GetMapping("/{issueNumberOrKey}")
    public ResponseEntity<IssueDetailResponse> getIssue(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @PathVariable String issueNumberOrKey,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueApplicationFacade.getIssue(platform, repositoryId, issueNumberOrKey, session));
    }

    @PatchMapping("/{issueNumberOrKey}")
    public ResponseEntity<IssueDetailResponse> updateIssue(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @PathVariable String issueNumberOrKey,
        @RequestBody UpdateIssueRequest request,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueApplicationFacade.updateIssue(platform, repositoryId, issueNumberOrKey, request, session));
    }

    @DeleteMapping("/{issueNumberOrKey}")
    public ResponseEntity<Void> deleteIssue(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @PathVariable String issueNumberOrKey,
        HttpSession session
    ) {
        issueApplicationFacade.deleteIssue(platform, repositoryId, issueNumberOrKey, session);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{issueNumberOrKey}/sync-state")
    public ResponseEntity<SyncStateResponse> getIssueSyncState(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @PathVariable String issueNumberOrKey,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueApplicationFacade.getIssueSyncState(platform, repositoryId, issueNumberOrKey, session));
    }
}
