package com.jw.github_issue_manager.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.dto.issue.IssueResponse;
import com.jw.github_issue_manager.dto.label.CreateLabelRequest;
import com.jw.github_issue_manager.dto.label.LabelResponse;
import com.jw.github_issue_manager.service.IssueService;
import com.jw.github_issue_manager.service.LabelService;

import jakarta.validation.Valid;

@RestController
public class LabelController {

    private final LabelService labelService;
    private final IssueService issueService;

    public LabelController(LabelService labelService, IssueService issueService) {
        this.labelService = labelService;
        this.issueService = issueService;
    }

    @PostMapping("/api/repositories/{repositoryId}/labels")
    public ResponseEntity<LabelResponse> create(
        @PathVariable Long repositoryId,
        @Valid @RequestBody CreateLabelRequest request
    ) {
        LabelResponse response = labelService.create(repositoryId, request);
        return ResponseEntity.created(URI.create("/api/repositories/" + repositoryId + "/labels/" + response.id()))
            .body(response);
    }

    @GetMapping("/api/repositories/{repositoryId}/labels")
    public ResponseEntity<List<LabelResponse>> findAll(@PathVariable Long repositoryId) {
        return ResponseEntity.ok(labelService.findAll(repositoryId));
    }

    @PostMapping("/api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}")
    public ResponseEntity<IssueResponse> attachLabel(
        @PathVariable Long repositoryId,
        @PathVariable Long issueId,
        @PathVariable Long labelId
    ) {
        return ResponseEntity.ok(issueService.attachLabel(repositoryId, issueId, labelId));
    }

    @DeleteMapping("/api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}")
    public ResponseEntity<Void> detachLabel(
        @PathVariable Long repositoryId,
        @PathVariable Long issueId,
        @PathVariable Long labelId
    ) {
        issueService.detachLabel(repositoryId, issueId, labelId);
        return ResponseEntity.noContent().build();
    }
}
