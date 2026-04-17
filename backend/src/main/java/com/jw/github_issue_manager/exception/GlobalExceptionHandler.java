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

import com.jw.github_issue_manager.github.GitHubApiException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PLATFORM_API_ERROR = "PLATFORM_API_ERROR";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(exception.getCode(), exception.getMessage()));
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

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<ErrorResponse> handleGitHubApi(GitHubApiException exception) {
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
}
