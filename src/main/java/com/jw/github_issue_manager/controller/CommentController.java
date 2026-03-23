package com.jw.github_issue_manager.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.dto.comment.CommentResponse;
import com.jw.github_issue_manager.dto.comment.CreateCommentRequest;
import com.jw.github_issue_manager.service.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/issues/{issueId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
        @PathVariable Long repositoryId,
        @PathVariable Long issueId,
        @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse response = commentService.create(repositoryId, issueId, request);
        return ResponseEntity.created(URI.create(
            "/api/repositories/" + repositoryId + "/issues/" + issueId + "/comments/" + response.id()
        )).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> findAll(@PathVariable Long repositoryId, @PathVariable Long issueId) {
        return ResponseEntity.ok(commentService.findAll(repositoryId, issueId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long repositoryId,
        @PathVariable Long issueId,
        @PathVariable Long commentId
    ) {
        commentService.delete(repositoryId, issueId, commentId);
        return ResponseEntity.noContent().build();
    }
}
