package com.jw.github_issue_manager.exception;

public class PlatformIntegrationException extends RuntimeException {

    public PlatformIntegrationException(String message) {
        super(message);
    }

    public PlatformIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
