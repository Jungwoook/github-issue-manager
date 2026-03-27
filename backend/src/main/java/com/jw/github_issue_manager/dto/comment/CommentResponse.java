package com.jw.github_issue_manager.dto.comment;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.domain.Comment;
import com.jw.github_issue_manager.dto.user.UserSummaryResponse;

public record CommentResponse(
    Long id,
    Long issueId,
    String content,
    UserSummaryResponse author,
    LocalDateTime createdAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getIssue().getId(),
            comment.getContent(),
            UserSummaryResponse.from(comment.getAuthor()),
            comment.getCreatedAt()
        );
    }
}
