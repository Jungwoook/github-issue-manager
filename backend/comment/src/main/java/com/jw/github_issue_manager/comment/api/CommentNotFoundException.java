package com.jw.github_issue_manager.comment.api;

public class CommentNotFoundException extends RuntimeException {

    private static final String CODE = "COMMENT_NOT_FOUND";

    public CommentNotFoundException() {
        super("Comment was not found.");
    }

    public String getCode() {
        return CODE;
    }
}
