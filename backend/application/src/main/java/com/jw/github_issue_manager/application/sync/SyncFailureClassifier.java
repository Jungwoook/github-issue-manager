package com.jw.github_issue_manager.application.sync;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.jw.github_issue_manager.application.sync.failure.SyncFailureType;

@Component
public class SyncFailureClassifier {

    public SyncFailureType classify(RuntimeException exception) {
        if (exception instanceof ResourceAccessException) {
            return SyncFailureType.NETWORK_ERROR;
        }
        if (exception instanceof RestClientResponseException responseException) {
            int status = responseException.getStatusCode().value();
            if (status == 401) {
                return SyncFailureType.AUTHENTICATION_FAILED;
            }
            if (status == 403) {
                return SyncFailureType.PERMISSION_DENIED;
            }
            if (status == 404) {
                return SyncFailureType.REMOTE_NOT_FOUND;
            }
            if (status == 422) {
                return SyncFailureType.VALIDATION_FAILED;
            }
            if (status == 429 || status >= 500) {
                return SyncFailureType.TEMPORARY_UNAVAILABLE;
            }
        }
        return SyncFailureType.UNKNOWN;
    }

    public boolean isRetryable(SyncFailureType type) {
        return type == SyncFailureType.NETWORK_ERROR
            || type == SyncFailureType.TEMPORARY_UNAVAILABLE
            || type == SyncFailureType.RATE_LIMITED
            || type == SyncFailureType.UNKNOWN;
    }

    public String message(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
