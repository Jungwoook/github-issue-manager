package com.jw.github_issue_manager.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.domain.Issue;
import com.jw.github_issue_manager.domain.IssuePriority;
import com.jw.github_issue_manager.domain.IssueStatus;
import com.jw.github_issue_manager.domain.Label;
import com.jw.github_issue_manager.domain.RepositoryEntity;
import com.jw.github_issue_manager.domain.User;
import com.jw.github_issue_manager.dto.issue.CreateIssueRequest;
import com.jw.github_issue_manager.dto.issue.IssueResponse;
import com.jw.github_issue_manager.dto.issue.IssueSummaryResponse;
import com.jw.github_issue_manager.dto.issue.UpdateIssueAssigneeRequest;
import com.jw.github_issue_manager.dto.issue.UpdateIssuePriorityRequest;
import com.jw.github_issue_manager.dto.issue.UpdateIssueRequest;
import com.jw.github_issue_manager.dto.issue.UpdateIssueStatusRequest;
import com.jw.github_issue_manager.exception.IssueNotFoundException;
import com.jw.github_issue_manager.exception.LabelAlreadyAttachedException;
import com.jw.github_issue_manager.exception.LabelNotFoundException;
import com.jw.github_issue_manager.repository.IssueRepository;
import com.jw.github_issue_manager.repository.LabelRepository;

@Service
@Transactional(readOnly = true)
public class IssueService {

    private final IssueRepository issueRepository;
    private final LabelRepository labelRepository;
    private final RepositoryService repositoryService;
    private final UserService userService;

    public IssueService(
        IssueRepository issueRepository,
        LabelRepository labelRepository,
        RepositoryService repositoryService,
        UserService userService
    ) {
        this.issueRepository = issueRepository;
        this.labelRepository = labelRepository;
        this.repositoryService = repositoryService;
        this.userService = userService;
    }

    @Transactional
    public IssueResponse create(Long repositoryId, CreateIssueRequest request) {
        RepositoryEntity repository = repositoryService.getRepositoryEntity(repositoryId);
        User assignee = request.assigneeId() == null ? null : userService.getUser(request.assigneeId());
        Issue issue = new Issue(
            repository,
            request.title().trim(),
            request.content(),
            request.priority() == null ? IssuePriority.MEDIUM : request.priority(),
            assignee
        );
        return IssueResponse.from(issueRepository.save(issue));
    }

    public List<IssueSummaryResponse> findAll(
        Long repositoryId,
        String keyword,
        IssueStatus status,
        IssuePriority priority,
        Long labelId,
        Long assigneeId
    ) {
        RepositoryEntity repository = repositoryService.getRepositoryEntity(repositoryId);
        String normalizedKeyword = keyword == null ? null : keyword.trim().toLowerCase(Locale.ROOT);
        return issueRepository.findByRepository(repository)
            .stream()
            .filter(issue -> normalizedKeyword == null || normalizedKeyword.isBlank()
                || issue.getTitle().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
            .filter(issue -> status == null || issue.getStatus() == status)
            .filter(issue -> priority == null || issue.getPriority() == priority)
            .filter(issue -> labelId == null || issue.getLabels().stream().anyMatch(label -> labelId.equals(label.getId())))
            .filter(issue -> assigneeId == null || (issue.getAssignee() != null && assigneeId.equals(issue.getAssignee().getId())))
            .map(IssueSummaryResponse::from)
            .toList();
    }

    public IssueResponse findById(Long repositoryId, Long issueId) {
        return IssueResponse.from(getIssue(repositoryId, issueId));
    }

    @Transactional
    public IssueResponse update(Long repositoryId, Long issueId, UpdateIssueRequest request) {
        Issue issue = getIssue(repositoryId, issueId);
        User assignee = request.assigneeId() == null ? null : userService.getUser(request.assigneeId());
        issue.update(request.title().trim(), request.content(), assignee);
        return IssueResponse.from(issue);
    }

    @Transactional
    public void delete(Long repositoryId, Long issueId) {
        issueRepository.delete(getIssue(repositoryId, issueId));
    }

    @Transactional
    public IssueResponse updateStatus(Long repositoryId, Long issueId, UpdateIssueStatusRequest request) {
        Issue issue = getIssue(repositoryId, issueId);
        issue.updateStatus(request.status());
        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse updatePriority(Long repositoryId, Long issueId, UpdateIssuePriorityRequest request) {
        Issue issue = getIssue(repositoryId, issueId);
        issue.updatePriority(request.priority());
        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse updateAssignee(Long repositoryId, Long issueId, UpdateIssueAssigneeRequest request) {
        Issue issue = getIssue(repositoryId, issueId);
        User assignee = request.assigneeId() == null ? null : userService.getUser(request.assigneeId());
        issue.updateAssignee(assignee);
        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse attachLabel(Long repositoryId, Long issueId, Long labelId) {
        Issue issue = getIssue(repositoryId, issueId);
        Label label = labelRepository.findByIdAndRepositoryId(labelId, repositoryId)
            .orElseThrow(() -> new LabelNotFoundException(labelId));
        boolean alreadyAttached = issue.getLabels().stream().anyMatch(existing -> existing.getId().equals(labelId));
        if (alreadyAttached) {
            throw new LabelAlreadyAttachedException(issueId, labelId);
        }
        issue.addLabel(label);
        return IssueResponse.from(issue);
    }

    @Transactional
    public void detachLabel(Long repositoryId, Long issueId, Long labelId) {
        Issue issue = getIssue(repositoryId, issueId);
        Label label = labelRepository.findByIdAndRepositoryId(labelId, repositoryId)
            .orElseThrow(() -> new LabelNotFoundException(labelId));
        issue.removeLabel(label);
    }

    public Issue getIssue(Long repositoryId, Long issueId) {
        repositoryService.getRepositoryEntity(repositoryId);
        return issueRepository.findByIdAndRepositoryId(issueId, repositoryId)
            .orElseThrow(() -> new IssueNotFoundException(issueId));
    }
}
