package com.jw.github_issue_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.application.repository.RepositoryApplicationFacade;
import com.jw.github_issue_manager.application.sync.SyncStateResponse;
import com.jw.github_issue_manager.repository.api.dto.RepositoryResponse;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/platforms/{platform}/repositories")
public class RepositoryController {

    private final RepositoryApplicationFacade repositoryApplicationFacade;

    public RepositoryController(RepositoryApplicationFacade repositoryApplicationFacade) {
        this.repositoryApplicationFacade = repositoryApplicationFacade;
    }

    @GetMapping
    public ResponseEntity<List<RepositoryResponse>> getRepositories(@PathVariable String platform, HttpSession session) {
        return ResponseEntity.ok(repositoryApplicationFacade.getRepositories(platform, session));
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<RepositoryResponse>> refreshRepositories(@PathVariable String platform, HttpSession session) {
        return ResponseEntity.ok(repositoryApplicationFacade.refreshRepositories(platform, session));
    }

    @GetMapping("/{repositoryId}")
    public ResponseEntity<RepositoryResponse> getRepository(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        HttpSession session
    ) {
        return ResponseEntity.ok(repositoryApplicationFacade.getRepository(platform, repositoryId, session));
    }

    @GetMapping("/{repositoryId}/sync-state")
    public ResponseEntity<SyncStateResponse> getSyncState(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        HttpSession session
    ) {
        return ResponseEntity.ok(repositoryApplicationFacade.getRepositorySyncState(platform, repositoryId, session));
    }
}
