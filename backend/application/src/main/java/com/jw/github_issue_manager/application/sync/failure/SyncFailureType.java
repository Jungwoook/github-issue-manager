package com.jw.github_issue_manager.application.sync.failure;

public enum SyncFailureType {
    RATE_LIMITED,
    TEMPORARY_UNAVAILABLE,
    AUTHENTICATION_FAILED,
    PERMISSION_DENIED,
    REMOTE_NOT_FOUND,
    VALIDATION_FAILED,
    NETWORK_ERROR,
    UNKNOWN
}
