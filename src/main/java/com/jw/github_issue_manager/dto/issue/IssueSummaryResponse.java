package com.jw.github_issue_manager.dto.issue;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.domain.Issue;
import com.jw.github_issue_manager.domain.IssuePriority;
import com.jw.github_issue_manager.domain.IssueStatus;
import com.jw.github_issue_manager.dto.user.UserSummaryResponse;

public record IssueSummaryResponse(
    Long id,
    Long repositoryId,
    String title,
    IssueStatus status,
    IssuePriority priority,
    UserSummaryResponse assignee,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static IssueSummaryResponse from(Issue issue) {
        return new IssueSummaryResponse(
            issue.getId(),
            issue.getRepository().getId(),
            issue.getTitle(),
            issue.getStatus(),
            issue.getPriority(),
            issue.getAssignee() == null ? null : UserSummaryResponse.from(issue.getAssignee()),
            issue.getCreatedAt(),
            issue.getUpdatedAt()
        );
    }
}
