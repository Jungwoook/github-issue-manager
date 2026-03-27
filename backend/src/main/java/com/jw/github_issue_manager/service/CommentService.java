package com.jw.github_issue_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.domain.Comment;
import com.jw.github_issue_manager.domain.Issue;
import com.jw.github_issue_manager.domain.User;
import com.jw.github_issue_manager.dto.comment.CommentResponse;
import com.jw.github_issue_manager.dto.comment.CreateCommentRequest;
import com.jw.github_issue_manager.exception.CommentNotFoundException;
import com.jw.github_issue_manager.repository.CommentRepository;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueService issueService;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, IssueService issueService, UserService userService) {
        this.commentRepository = commentRepository;
        this.issueService = issueService;
        this.userService = userService;
    }

    @Transactional
    public CommentResponse create(Long repositoryId, Long issueId, CreateCommentRequest request) {
        Issue issue = issueService.getIssue(repositoryId, issueId);
        User author = userService.getUser(request.authorId());
        Comment comment = new Comment(issue, author, request.content().trim());
        return CommentResponse.from(commentRepository.save(comment));
    }

    public List<CommentResponse> findAll(Long repositoryId, Long issueId) {
        Issue issue = issueService.getIssue(repositoryId, issueId);
        return commentRepository.findByIssue(issue).stream().map(CommentResponse::from).toList();
    }

    @Transactional
    public void delete(Long repositoryId, Long issueId, Long commentId) {
        issueService.getIssue(repositoryId, issueId);
        Comment comment = commentRepository.findByIdAndIssueId(commentId, issueId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
        commentRepository.delete(comment);
    }
}
