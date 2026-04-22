package com.jw.github_issue_manager.issue.api.dto;

public record UpdateIssueRequest(
    String title,
    String body,
    String state
) {
}
