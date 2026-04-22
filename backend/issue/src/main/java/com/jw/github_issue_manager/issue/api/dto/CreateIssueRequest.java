package com.jw.github_issue_manager.issue.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateIssueRequest(
    @NotBlank String title,
    String body
) {
}
