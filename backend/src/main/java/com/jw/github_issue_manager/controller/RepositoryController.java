package com.jw.github_issue_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.service.RepositoryService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping
    public ResponseEntity<List<RepositoryResponse>> getRepositories(HttpSession session) {
        return ResponseEntity.ok(repositoryService.getRepositories(session));
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<RepositoryResponse>> refreshRepositories(HttpSession session) {
        return ResponseEntity.ok(repositoryService.refreshRepositories(session));
    }

    @GetMapping("/{repositoryId}")
    public ResponseEntity<RepositoryResponse> getRepository(@PathVariable Long repositoryId, HttpSession session) {
        return ResponseEntity.ok(repositoryService.getRepository(repositoryId, session));
    }

    @GetMapping("/{repositoryId}/sync-state")
    public ResponseEntity<SyncStateResponse> getSyncState(@PathVariable Long repositoryId, HttpSession session) {
        return ResponseEntity.ok(repositoryService.getRepositorySyncState(repositoryId, session));
    }
}
