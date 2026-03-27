package com.jw.github_issue_manager.dto.issue;

import java.time.LocalDateTime;
import java.util.List;

import com.jw.github_issue_manager.domain.Issue;
import com.jw.github_issue_manager.domain.IssuePriority;
import com.jw.github_issue_manager.domain.IssueStatus;
import com.jw.github_issue_manager.dto.user.UserSummaryResponse;

public record IssueResponse(
    Long id,
    Long repositoryId,
    String title,
    String content,
    IssueStatus status,
    IssuePriority priority,
    UserSummaryResponse assignee,
    List<LabelSummaryResponse> labels,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static IssueResponse from(Issue issue) {
        return new IssueResponse(
            issue.getId(),
            issue.getRepository().getId(),
            issue.getTitle(),
            issue.getContent(),
            issue.getStatus(),
            issue.getPriority(),
            issue.getAssignee() == null ? null : UserSummaryResponse.from(issue.getAssignee()),
            issue.getLabels().stream().map(LabelSummaryResponse::from).toList(),
            issue.getCreatedAt(),
            issue.getUpdatedAt()
        );
    }
}
