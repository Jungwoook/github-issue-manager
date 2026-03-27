package com.jw.github_issue_manager.dto.issue;

import com.jw.github_issue_manager.domain.IssueStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateIssueStatusRequest(
    @NotNull(message = "status must not be null")
    IssueStatus status
) {
}
