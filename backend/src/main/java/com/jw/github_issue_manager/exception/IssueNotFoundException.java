package com.jw.github_issue_manager.exception;

public class IssueNotFoundException extends RuntimeException {

    public IssueNotFoundException(Long issueId) {
        super("Issue not found. id=" + issueId);
    }
}
