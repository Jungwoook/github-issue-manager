package com.jw.github_issue_manager.comment.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.comment.api.dto.CommentResponse;
import com.jw.github_issue_manager.comment.internal.service.CommentService;

@Service
public class CommentFacade {

    private final CommentService commentService;

    public CommentFacade(CommentService commentService) {
        this.commentService = commentService;
    }

    public List<CommentResponse> getComments(
        PlatformType platform,
        String issueExternalId
    ) {
        return commentService.getComments(platform, issueExternalId);
    }

    public List<CommentResponse> upsertComments(
        PlatformType platform,
        String issueExternalId,
        List<RemoteComment> comments
    ) {
        return commentService.upsertComments(platform, issueExternalId, comments);
    }

    public CommentResponse upsertComment(
        PlatformType platform,
        String issueExternalId,
        RemoteComment comment
    ) {
        return commentService.upsertComment(platform, issueExternalId, comment);
    }
}
