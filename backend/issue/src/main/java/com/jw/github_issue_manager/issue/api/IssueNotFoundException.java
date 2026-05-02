package com.jw.github_issue_manager.issue.api;

public class IssueNotFoundException extends RuntimeException {

    private static final String CODE = "ISSUE_NOT_FOUND";

    public IssueNotFoundException() {
        super("Issue was not found.");
    }

    public String getCode() {
        return CODE;
    }
}
