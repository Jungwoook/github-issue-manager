package com.jw.github_issue_manager.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    List<FieldErrorDetail> errors,
    LocalDateTime timestamp
) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of(), LocalDateTime.now());
    }

    public static ErrorResponse of(String code, String message, List<FieldErrorDetail> errors) {
        return new ErrorResponse(code, message, errors, LocalDateTime.now());
    }

    public record FieldErrorDetail(String field, String reason) {
    }
}
