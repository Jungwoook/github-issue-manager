package com.jw.github_issue_manager.exception;

public class DuplicateLabelNameException extends RuntimeException {

    public DuplicateLabelNameException(String name) {
        super("Label name already exists. name=" + name);
    }
}
