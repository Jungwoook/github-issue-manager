package com.jw.github_issue_manager.dto.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateIssueRequest(
    @NotBlank(message = "title must not be blank")
    @Size(max = 200, message = "title must be at most 200 characters")
    String title,

    String content,

    Long assigneeId
) {
}
