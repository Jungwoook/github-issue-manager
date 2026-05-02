package com.jw.github_issue_manager.repository.api;

public class RepositoryNotFoundException extends RuntimeException {

    private static final String CODE = "REPOSITORY_NOT_FOUND";

    public RepositoryNotFoundException() {
        super("Repository was not found.");
    }

    public String getCode() {
        return CODE;
    }
}
