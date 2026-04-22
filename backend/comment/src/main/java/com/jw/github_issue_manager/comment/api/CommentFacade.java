package com.jw.github_issue_manager.comment.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.comment.api.dto.CommentResponse;
import com.jw.github_issue_manager.comment.api.dto.CreateCommentRequest;
import com.jw.github_issue_manager.comment.internal.service.CommentService;

import jakarta.servlet.http.HttpSession;

@Service
public class CommentFacade {

    private final CommentService commentService;

    public CommentFacade(CommentService commentService) {
        this.commentService = commentService;
    }

    public List<CommentResponse> getComments(
        PlatformType platform,
        String repositoryId,
        String issueNumberOrKey,
        HttpSession session
    ) {
        return commentService.getComments(platform, repositoryId, issueNumberOrKey, session);
    }

    public List<CommentResponse> refreshComments(
        PlatformType platform,
        String repositoryId,
        String issueNumberOrKey,
        HttpSession session
    ) {
        return commentService.refreshComments(platform, repositoryId, issueNumberOrKey, session);
    }

    public CommentResponse createComment(
        PlatformType platform,
        String repositoryId,
        String issueNumberOrKey,
        CreateCommentRequest request,
        HttpSession session
    ) {
        return commentService.createComment(platform, repositoryId, issueNumberOrKey, request, session);
    }
}
