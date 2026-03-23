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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RepositoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryNotFound(RepositoryNotFoundException exception) {
        return conflictOrNotFound(HttpStatus.NOT_FOUND, "REPOSITORY_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        return conflictOrNotFound(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(IssueNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIssueNotFound(IssueNotFoundException exception) {
        return conflictOrNotFound(HttpStatus.NOT_FOUND, "ISSUE_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFound(CommentNotFoundException exception) {
        return conflictOrNotFound(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(LabelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLabelNotFound(LabelNotFoundException exception) {
        return conflictOrNotFound(HttpStatus.NOT_FOUND, "LABEL_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(DuplicateUserUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(DuplicateUserUsernameException exception) {
        return conflictOrNotFound(HttpStatus.CONFLICT, "DUPLICATE_USER_USERNAME", exception.getMessage());
    }

    @ExceptionHandler(DuplicateUserEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateUserEmailException exception) {
        return conflictOrNotFound(HttpStatus.CONFLICT, "DUPLICATE_USER_EMAIL", exception.getMessage());
    }

    @ExceptionHandler(UserDeleteConflictException.class)
    public ResponseEntity<ErrorResponse> handleUserDeleteConflict(UserDeleteConflictException exception) {
        return conflictOrNotFound(HttpStatus.CONFLICT, "USER_DELETE_CONFLICT", exception.getMessage());
    }

    @ExceptionHandler(DuplicateLabelNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLabelName(DuplicateLabelNameException exception) {
        return conflictOrNotFound(HttpStatus.CONFLICT, "DUPLICATE_LABEL_NAME", exception.getMessage());
    }

    @ExceptionHandler(LabelAlreadyAttachedException.class)
    public ResponseEntity<ErrorResponse> handleLabelAlreadyAttached(LabelAlreadyAttachedException exception) {
        return conflictOrNotFound(HttpStatus.CONFLICT, "LABEL_ALREADY_ATTACHED", exception.getMessage());
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

    private ResponseEntity<ErrorResponse> conflictOrNotFound(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(code, message));
    }

    private ErrorResponse.FieldErrorDetail toFieldErrorDetail(FieldError fieldError) {
        return new ErrorResponse.FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
