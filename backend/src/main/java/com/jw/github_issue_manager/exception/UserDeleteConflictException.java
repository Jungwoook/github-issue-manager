package com.jw.github_issue_manager.exception;

public class UserDeleteConflictException extends RuntimeException {

    public UserDeleteConflictException(Long userId) {
        super("User cannot be deleted because it is referenced. id=" + userId);
    }
}
