package com.jw.github_issue_manager.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.domain.IssuePriority;
import com.jw.github_issue_manager.domain.IssueStatus;
import com.jw.github_issue_manager.dto.issue.CreateIssueRequest;
import com.jw.github_issue_manager.dto.issue.IssueResponse;
import com.jw.github_issue_manager.dto.issue.IssueSummaryResponse;
import com.jw.github_issue_manager.dto.issue.UpdateIssueAssigneeRequest;
import com.jw.github_issue_manager.dto.issue.UpdateIssuePriorityRequest;
import com.jw.github_issue_manager.dto.issue.UpdateIssueRequest;
import com.jw.github_issue_manager.dto.issue.UpdateIssueStatusRequest;
import com.jw.github_issue_manager.service.IssueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    public ResponseEntity<IssueResponse> create(
        @PathVariable Long repositoryId,
        @Valid @RequestBody CreateIssueRequest request
    ) {
        IssueResponse response = issueService.create(repositoryId, request);
        return ResponseEntity.created(URI.create("/api/repositories/" + repositoryId + "/issues/" + response.id()))
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<IssueSummaryResponse>> findAll(
        @PathVariable Long repositoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) IssueStatus status,
        @RequestParam(required = false) IssuePriority priority,
        @RequestParam(required = false) Long labelId,
        @RequestParam(required = false) Long assigneeId
    ) {
        return ResponseEntity.ok(issueService.findAll(repositoryId, keyword, status, priority, labelId, assigneeId));
    }

    @GetMapping("/{issueId}")
    public ResponseEntity<IssueResponse> findById(@PathVariable Long repositoryId, @PathVariable Long issueId) {
        return ResponseEntity.ok(issueService.findById(repositoryId, issueId));
    }

    @PutMapping("/{issueId}")
    public ResponseEntity<IssueResponse> update(
        @PathVariable Long repositoryId,
        @PathVariable Long issueId,
        @Valid @RequestBody UpdateIssueRequest request
    ) {
        return ResponseEntity.ok(issueService.update(repositoryId, issueId, request));
    }

    @DeleteMapping("/{issueId}")
    public ResponseEntity<Void> delete(@PathVariable Long repositoryId, @PathVariable Long issueId) {
        issueService.delete(repositoryId, issueId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{issueId}/status")
    public ResponseEntity<IssueResponse> updateStatus(
        @PathVariable Long repositoryId,
        @PathVariable Long issueId,
        @Valid @RequestBody UpdateIssueStatusRequest request
    ) {
        return ResponseEntity.ok(issueService.updateStatus(repositoryId, issueId, request));
    }

    @PatchMapping("/{issueId}/priority")
    public ResponseEntity<IssueResponse> updatePriority(
        @PathVariable Long repositoryId,
        @PathVariable Long issueId,
        @Valid @RequestBody UpdateIssuePriorityRequest request
    ) {
        return ResponseEntity.ok(issueService.updatePriority(repositoryId, issueId, request));
    }

    @PatchMapping("/{issueId}/assignee")
    public ResponseEntity<IssueResponse> updateAssignee(
        @PathVariable Long repositoryId,
        @PathVariable Long issueId,
        @RequestBody UpdateIssueAssigneeRequest request
    ) {
        return ResponseEntity.ok(issueService.updateAssignee(repositoryId, issueId, request));
    }
}
