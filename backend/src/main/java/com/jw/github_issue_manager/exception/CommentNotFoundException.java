package com.jw.github_issue_manager.exception;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(Long commentId) {
        super("Comment not found. id=" + commentId);
    }
}
