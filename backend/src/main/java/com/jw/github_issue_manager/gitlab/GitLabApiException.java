package com.jw.github_issue_manager.gitlab;

public class GitLabApiException extends RuntimeException {

    public GitLabApiException(String message) {
        super(message);
    }
}
