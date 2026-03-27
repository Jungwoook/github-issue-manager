package com.jw.github_issue_manager.dto.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRepositoryRequest(
    @NotBlank(message = "name must not be blank")
    @Size(max = 100, message = "name must be at most 100 characters")
    String name,

    @Size(max = 1000, message = "description must be at most 1000 characters")
    String description
) {
}
