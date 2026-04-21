package com.jw.github_issue_manager.dto.issue;

import jakarta.validation.constraints.NotBlank;

public record CreateIssueRequest(
    @NotBlank String title,
    String body
) {
}
