package com.jw.github_issue_manager.dto.user;

import com.jw.github_issue_manager.domain.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank(message = "displayName must not be blank")
    @Size(max = 100, message = "displayName must be at most 100 characters")
    String displayName,

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a well-formed email address")
    @Size(max = 255, message = "email must be at most 255 characters")
    String email,

    @NotNull(message = "role must not be null")
    UserRole role
) {
}
