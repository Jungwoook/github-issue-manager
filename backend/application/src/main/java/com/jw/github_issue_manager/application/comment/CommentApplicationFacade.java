package com.jw.github_issue_manager.application.comment;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.comment.api.CommentFacade;
import com.jw.github_issue_manager.comment.api.dto.CommentResponse;
import com.jw.github_issue_manager.comment.api.dto.CreateCommentRequest;
import com.jw.github_issue_manager.core.platform.PlatformType;

import jakarta.servlet.http.HttpSession;

@Service
public class CommentApplicationFacade {

    private final CommentFacade commentFacade;

    public CommentApplicationFacade(CommentFacade commentFacade) {
        this.commentFacade = commentFacade;
    }

    public List<CommentResponse> getComments(
        String platform,
        String repositoryId,
        String issueNumberOrKey,
        HttpSession session
    ) {
        return commentFacade.getComments(PlatformType.from(platform), repositoryId, issueNumberOrKey, session);
    }

    public List<CommentResponse> refreshComments(
        String platform,
        String repositoryId,
        String issueNumberOrKey,
        HttpSession session
    ) {
        return commentFacade.refreshComments(PlatformType.from(platform), repositoryId, issueNumberOrKey, session);
    }

    public CommentResponse createComment(
        String platform,
        String repositoryId,
        String issueNumberOrKey,
        CreateCommentRequest request,
        HttpSession session
    ) {
        return commentFacade.createComment(PlatformType.from(platform), repositoryId, issueNumberOrKey, request, session);
    }
}
