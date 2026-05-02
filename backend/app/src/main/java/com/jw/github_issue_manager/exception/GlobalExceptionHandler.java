package com.jw.github_issue_manager.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import com.jw.github_issue_manager.comment.api.CommentNotFoundException;
import com.jw.github_issue_manager.github.GitHubApiException;
import com.jw.github_issue_manager.gitlab.GitLabApiException;
import com.jw.github_issue_manager.issue.api.IssueNotFoundException;
import com.jw.github_issue_manager.repository.api.RepositoryNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PLATFORM_API_ERROR = "PLATFORM_API_ERROR";

    @ExceptionHandler(RepositoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryNotFound(RepositoryNotFoundException exception) {
        return notFound(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(IssueNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIssueNotFound(IssueNotFoundException exception) {
        return notFound(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFound(CommentNotFoundException exception) {
        return notFound(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.of("UNAUTHORIZED", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<ErrorResponse.FieldErrorDetail> errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toFieldErrorDetail)
            .toList();

        return ResponseEntity.badRequest()
            .body(ErrorResponse.of("VALIDATION_ERROR", "Request validation failed", errors));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of("VALIDATION_ERROR", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of("VALIDATION_ERROR", exception.getMessage()));
    }

    @ExceptionHandler({GitHubApiException.class, GitLabApiException.class})
    public ResponseEntity<ErrorResponse> handlePlatformApi(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(ErrorResponse.of(PLATFORM_API_ERROR, exception.getMessage()));
    }

    @ExceptionHandler(PlatformIntegrationException.class)
    public ResponseEntity<ErrorResponse> handlePlatformIntegration(PlatformIntegrationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(ErrorResponse.of("PLATFORM_INTEGRATION_ERROR", exception.getMessage()));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleGitHubHttp(RestClientResponseException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        HttpStatus responseStatus = status == null || status.is5xxServerError() ? HttpStatus.BAD_GATEWAY : status;
        String message = exception.getResponseBodyAsString();

        if (message == null || message.isBlank()) {
            message = exception.getStatusText();
        }

        return ResponseEntity.status(responseStatus)
            .body(ErrorResponse.of(PLATFORM_API_ERROR, message));
    }

    private ErrorResponse.FieldErrorDetail toFieldErrorDetail(FieldError fieldError) {
        return new ErrorResponse.FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> notFound(String code, String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(code, message));
    }
}
