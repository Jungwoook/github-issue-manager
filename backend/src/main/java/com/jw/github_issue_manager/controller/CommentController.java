package com.jw.github_issue_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.dto.comment.CommentResponse;
import com.jw.github_issue_manager.dto.comment.CreateCommentRequest;
import com.jw.github_issue_manager.service.CommentService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @PathVariable String issueNumberOrKey,
        HttpSession session
    ) {
        return ResponseEntity.ok(commentService.getComments(PlatformType.from(platform), repositoryId, issueNumberOrKey, session));
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<CommentResponse>> refreshComments(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @PathVariable String issueNumberOrKey,
        HttpSession session
    ) {
        return ResponseEntity.ok(commentService.refreshComments(PlatformType.from(platform), repositoryId, issueNumberOrKey, session));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
        @PathVariable String platform,
        @PathVariable String repositoryId,
        @PathVariable String issueNumberOrKey,
        @Valid @RequestBody CreateCommentRequest request,
        HttpSession session
    ) {
        return ResponseEntity.ok(commentService.createComment(PlatformType.from(platform), repositoryId, issueNumberOrKey, request, session));
    }
}
