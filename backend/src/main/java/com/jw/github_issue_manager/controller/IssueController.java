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

import com.jw.github_issue_manager.dto.issue.CreateIssueRequest;
import com.jw.github_issue_manager.dto.issue.IssueDetailResponse;
import com.jw.github_issue_manager.dto.issue.IssueSummaryResponse;
import com.jw.github_issue_manager.dto.issue.UpdateIssueRequest;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.service.IssueService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public ResponseEntity<List<IssueSummaryResponse>> getIssues(
        @PathVariable Long repositoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String state,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueService.getIssues(repositoryId, keyword, state, session));
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<IssueSummaryResponse>> refreshIssues(@PathVariable Long repositoryId, HttpSession session) {
        return ResponseEntity.ok(issueService.refreshIssues(repositoryId, session));
    }

    @PostMapping
    public ResponseEntity<IssueDetailResponse> createIssue(
        @PathVariable Long repositoryId,
        @Valid @RequestBody CreateIssueRequest request,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueService.createIssue(repositoryId, request, session));
    }

    @GetMapping("/{issueNumber}")
    public ResponseEntity<IssueDetailResponse> getIssue(
        @PathVariable Long repositoryId,
        @PathVariable Integer issueNumber,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueService.getIssue(repositoryId, issueNumber, session));
    }

    @PatchMapping("/{issueNumber}")
    public ResponseEntity<IssueDetailResponse> updateIssue(
        @PathVariable Long repositoryId,
        @PathVariable Integer issueNumber,
        @RequestBody UpdateIssueRequest request,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueService.updateIssue(repositoryId, issueNumber, request, session));
    }

    @DeleteMapping("/{issueNumber}")
    public ResponseEntity<Void> deleteIssue(
        @PathVariable Long repositoryId,
        @PathVariable Integer issueNumber,
        HttpSession session
    ) {
        issueService.deleteIssue(repositoryId, issueNumber, session);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{issueNumber}/sync-state")
    public ResponseEntity<SyncStateResponse> getIssueSyncState(
        @PathVariable Long repositoryId,
        @PathVariable Integer issueNumber,
        HttpSession session
    ) {
        return ResponseEntity.ok(issueService.getIssueSyncState(repositoryId, issueNumber, session));
    }
}
