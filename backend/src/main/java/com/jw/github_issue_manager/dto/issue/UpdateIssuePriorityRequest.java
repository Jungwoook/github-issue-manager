package com.jw.github_issue_manager.dto.issue;

import com.jw.github_issue_manager.domain.IssuePriority;

import jakarta.validation.constraints.NotNull;

public record UpdateIssuePriorityRequest(
    @NotNull(message = "priority must not be null")
    IssuePriority priority
) {
}
