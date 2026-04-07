package com.jw.github_issue_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.dto.comment.CommentResponse;
import com.jw.github_issue_manager.dto.comment.CreateCommentRequest;
import com.jw.github_issue_manager.service.CommentService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/issues/{issueNumber}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
        @PathVariable Long repositoryId,
        @PathVariable Integer issueNumber,
        HttpSession session
    ) {
        return ResponseEntity.ok(commentService.getComments(repositoryId, issueNumber, session));
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<CommentResponse>> refreshComments(
        @PathVariable Long repositoryId,
        @PathVariable Integer issueNumber,
        HttpSession session
    ) {
        return ResponseEntity.ok(commentService.refreshComments(repositoryId, issueNumber, session));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
        @PathVariable Long repositoryId,
        @PathVariable Integer issueNumber,
        @Valid @RequestBody CreateCommentRequest request,
        HttpSession session
    ) {
        return ResponseEntity.ok(commentService.createComment(repositoryId, issueNumber, request, session));
    }
}
