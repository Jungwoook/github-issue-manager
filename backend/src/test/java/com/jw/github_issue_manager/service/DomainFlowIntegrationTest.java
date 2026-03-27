package com.jw.github_issue_manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.domain.IssuePriority;
import com.jw.github_issue_manager.domain.IssueStatus;
import com.jw.github_issue_manager.domain.UserRole;
import com.jw.github_issue_manager.dto.comment.CommentResponse;
import com.jw.github_issue_manager.dto.comment.CreateCommentRequest;
import com.jw.github_issue_manager.dto.issue.CreateIssueRequest;
import com.jw.github_issue_manager.dto.issue.IssueResponse;
import com.jw.github_issue_manager.dto.issue.IssueSummaryResponse;
import com.jw.github_issue_manager.dto.issue.UpdateIssueAssigneeRequest;
import com.jw.github_issue_manager.dto.issue.UpdateIssuePriorityRequest;
import com.jw.github_issue_manager.dto.issue.UpdateIssueStatusRequest;
import com.jw.github_issue_manager.dto.label.CreateLabelRequest;
import com.jw.github_issue_manager.dto.label.LabelResponse;
import com.jw.github_issue_manager.dto.repository.CreateRepositoryRequest;
import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.user.CreateUserRequest;
import com.jw.github_issue_manager.dto.user.UpdateUserRequest;
import com.jw.github_issue_manager.dto.user.UserResponse;
import com.jw.github_issue_manager.exception.UserDeleteConflictException;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class DomainFlowIntegrationTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private IssueService issueService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LabelService labelService;

    @Test
    void userIssueCommentLabelFlowWorks() {
        RepositoryResponse repository = repositoryService.create(
            new CreateRepositoryRequest("backend-service", "백엔드 이슈 관리 저장소")
        );

        UserResponse user = userService.create(
            new CreateUserRequest("jane.doe", "Jane Doe", "jane.doe@example.com", UserRole.MEMBER)
        );

        IssueResponse createdIssue = issueService.create(
            repository.id(),
            new CreateIssueRequest(
                "로그인 오류 수정",
                "OAuth 로그인 리다이렉트 처리에서 오류가 발생합니다.",
                IssuePriority.HIGH,
                user.id()
            )
        );

        assertThat(createdIssue.status()).isEqualTo(IssueStatus.OPEN);
        assertThat(createdIssue.assignee().username()).isEqualTo("jane.doe");

        List<IssueSummaryResponse> issues = issueService.findAll(
            repository.id(),
            "로그인",
            IssueStatus.OPEN,
            IssuePriority.HIGH,
            null,
            user.id()
        );
        assertThat(issues).hasSize(1);

        LabelResponse label = labelService.create(repository.id(), new CreateLabelRequest("bug", "#d73a4a"));
        IssueResponse labeledIssue = issueService.attachLabel(repository.id(), createdIssue.id(), label.id());
        assertThat(labeledIssue.labels()).hasSize(1);

        CommentResponse comment = commentService.create(
            repository.id(),
            createdIssue.id(),
            new CreateCommentRequest("재현 경로 확인 중입니다.", user.id())
        );
        assertThat(comment.author().displayName()).isEqualTo("Jane Doe");

        UserResponse updatedUser = userService.update(
            user.id(),
            new UpdateUserRequest("Jane Kim", "jane.kim@example.com", UserRole.ADMIN)
        );
        assertThat(updatedUser.role()).isEqualTo(UserRole.ADMIN);

        IssueResponse closedIssue = issueService.updateStatus(
            repository.id(),
            createdIssue.id(),
            new UpdateIssueStatusRequest(IssueStatus.CLOSED)
        );
        assertThat(closedIssue.status()).isEqualTo(IssueStatus.CLOSED);

        IssueResponse reprioritizedIssue = issueService.updatePriority(
            repository.id(),
            createdIssue.id(),
            new UpdateIssuePriorityRequest(IssuePriority.LOW)
        );
        assertThat(reprioritizedIssue.priority()).isEqualTo(IssuePriority.LOW);

        IssueResponse unassignedIssue = issueService.updateAssignee(
            repository.id(),
            createdIssue.id(),
            new UpdateIssueAssigneeRequest(null)
        );
        assertThat(unassignedIssue.assignee()).isNull();

        assertThatThrownBy(() -> userService.delete(user.id()))
            .isInstanceOf(UserDeleteConflictException.class);

        commentService.delete(repository.id(), createdIssue.id(), comment.id());
        issueService.detachLabel(repository.id(), createdIssue.id(), label.id());
        issueService.delete(repository.id(), createdIssue.id());
        userService.delete(user.id());

        assertThat(userService.findAll(null, null)).isEmpty();
    }
}
