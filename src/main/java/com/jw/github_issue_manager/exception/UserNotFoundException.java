package com.jw.github_issue_manager.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("User not found. id=" + userId);
    }
}
