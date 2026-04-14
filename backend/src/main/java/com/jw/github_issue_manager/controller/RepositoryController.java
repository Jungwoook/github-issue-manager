package com.jw.github_issue_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.service.RepositoryService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/platforms/{platform}/repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping
    public ResponseEntity<List<RepositoryResponse>> getRepositories(@PathVariable String platform, HttpSession session) {
        return ResponseEntity.ok(repositoryService.getRepositories(PlatformType.from(platform), session));
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<RepositoryResponse>> refreshRepositories(@PathVariable String platform, HttpSession session) {
        return ResponseEntity.ok(repositoryService.refreshRepositories(PlatformType.from(platform), session));
    }

    @GetMapping("/{repositoryId}")
    public ResponseEntity<RepositoryResponse> getRepository(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        HttpSession session
    ) {
        return ResponseEntity.ok(repositoryService.getRepository(PlatformType.from(platform), repositoryId, session));
    }

    @GetMapping("/{repositoryId}/sync-state")
    public ResponseEntity<SyncStateResponse> getSyncState(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        HttpSession session
    ) {
        return ResponseEntity.ok(repositoryService.getRepositorySyncState(PlatformType.from(platform), repositoryId, session));
    }
}
