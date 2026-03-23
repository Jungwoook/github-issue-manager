package com.jw.github_issue_manager.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.dto.repository.CreateRepositoryRequest;
import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.repository.UpdateRepositoryRequest;
import com.jw.github_issue_manager.service.RepositoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    public ResponseEntity<RepositoryResponse> create(@Valid @RequestBody CreateRepositoryRequest request) {
        RepositoryResponse response = repositoryService.create(request);
        return ResponseEntity.created(URI.create("/api/repositories/" + response.id()))
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RepositoryResponse>> findAll() {
        return ResponseEntity.ok(repositoryService.findAll());
    }

    @GetMapping("/{repositoryId}")
    public ResponseEntity<RepositoryResponse> findById(@PathVariable Long repositoryId) {
        return ResponseEntity.ok(repositoryService.findById(repositoryId));
    }

    @PutMapping("/{repositoryId}")
    public ResponseEntity<RepositoryResponse> update(
        @PathVariable Long repositoryId,
        @Valid @RequestBody UpdateRepositoryRequest request
    ) {
        return ResponseEntity.ok(repositoryService.update(repositoryId, request));
    }

    @DeleteMapping("/{repositoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long repositoryId) {
        repositoryService.delete(repositoryId);
        return ResponseEntity.noContent().build();
    }
}
