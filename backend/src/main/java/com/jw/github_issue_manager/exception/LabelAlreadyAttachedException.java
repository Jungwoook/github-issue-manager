package com.jw.github_issue_manager.exception;

public class LabelAlreadyAttachedException extends RuntimeException {

    public LabelAlreadyAttachedException(Long issueId, Long labelId) {
        super("Label is already attached. issueId=" + issueId + ", labelId=" + labelId);
    }
}
