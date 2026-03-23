package com.jw.github_issue_manager.dto.issue;

import com.jw.github_issue_manager.domain.IssuePriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIssueRequest(
    @NotBlank(message = "title must not be blank")
    @Size(max = 200, message = "title must be at most 200 characters")
    String title,

    String content,

    IssuePriority priority,

    Long assigneeId
) {
}
