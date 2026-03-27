package com.jw.github_issue_manager.exception;

public class DuplicateUserEmailException extends RuntimeException {

    public DuplicateUserEmailException(String email) {
        super("Email already exists. email=" + email);
    }
}
