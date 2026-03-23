package com.jw.github_issue_manager.exception;

public class RepositoryNotFoundException extends RuntimeException {

    public RepositoryNotFoundException(Long repositoryId) {
        super("Repository not found. id=" + repositoryId);
    }
}
