package com.jw.github_issue_manager.application.sync;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
            if (isRateLimited(responseException)) {
                return SyncFailureType.RATE_LIMITED;
            }
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

    public LocalDateTime nextRetryAt(RuntimeException exception) {
        if (!(exception instanceof RestClientResponseException responseException)) {
            return null;
        }

        Integer retryAfterSeconds = parseInteger(responseException.getResponseHeaders() == null ? null : responseException.getResponseHeaders().getFirst("Retry-After"));
        if (retryAfterSeconds != null) {
            return LocalDateTime.now().plusSeconds(retryAfterSeconds);
        }

        Long resetEpochSecond = parseLong(responseException.getResponseHeaders() == null ? null : responseException.getResponseHeaders().getFirst("X-RateLimit-Reset"));
        return resetEpochSecond == null ? null : LocalDateTime.ofInstant(Instant.ofEpochSecond(resetEpochSecond), ZoneId.systemDefault());
    }

    public boolean isRetryable(SyncFailureType type) {
        return type == SyncFailureType.NETWORK_ERROR
            || type == SyncFailureType.TEMPORARY_UNAVAILABLE
            || type == SyncFailureType.RATE_LIMITED;
    }

    public String message(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    private boolean isRateLimited(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        if (status == 429) {
            return true;
        }
        if (status != 403) {
            return false;
        }
        if (exception.getResponseHeaders() == null) {
            return false;
        }
        String remaining = exception.getResponseHeaders().getFirst("X-RateLimit-Remaining");
        return "0".equals(remaining);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
