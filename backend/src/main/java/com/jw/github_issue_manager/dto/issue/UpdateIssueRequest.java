package com.jw.github_issue_manager.dto.issue;

public record UpdateIssueRequest(
    String title,
    String body,
    String state
) {
}
