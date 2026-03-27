package com.jw.github_issue_manager.exception;

public class LabelNotFoundException extends RuntimeException {

    public LabelNotFoundException(Long labelId) {
        super("Label not found. id=" + labelId);
    }
}
