package com.jw.github_issue_manager.exception;

public class DuplicateUserUsernameException extends RuntimeException {

    public DuplicateUserUsernameException(String username) {
        super("Username already exists. username=" + username);
    }
}
