package com.jw.github_issue_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.domain.CommentCache;
import com.jw.github_issue_manager.domain.IssueCache;
import com.jw.github_issue_manager.domain.SyncResourceType;
import com.jw.github_issue_manager.dto.comment.CommentResponse;
import com.jw.github_issue_manager.dto.comment.CreateCommentRequest;
import com.jw.github_issue_manager.repository.CommentCacheRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class CommentService {

    private final CommentCacheRepository commentCacheRepository;
    private final IssueService issueService;
    private final RepositoryService repositoryService;
    private final SyncStateService syncStateService;
    private final AuthService authService;
    private final PlatformGatewayResolver platformGatewayResolver;

    public CommentService(
        CommentCacheRepository commentCacheRepository,
        IssueService issueService,
        RepositoryService repositoryService,
        SyncStateService syncStateService,
        AuthService authService,
        PlatformGatewayResolver platformGatewayResolver
    ) {
        this.commentCacheRepository = commentCacheRepository;
        this.issueService = issueService;
        this.repositoryService = repositoryService;
        this.syncStateService = syncStateService;
        this.authService = authService;
        this.platformGatewayResolver = platformGatewayResolver;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long githubRepositoryId, Integer issueNumber, HttpSession session) {
        IssueCache issue = issueService.requireIssue(githubRepositoryId, issueNumber, session);
        return commentCacheRepository.findByPlatformAndIssueExternalIdOrderByCreatedAtAsc(
                PlatformType.GITHUB,
                issue.getExternalId()
            ).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<CommentResponse> refreshComments(Long githubRepositoryId, Integer issueNumber, HttpSession session) {
        var repository = repositoryService.requireAccessibleRepository(githubRepositoryId, session);
        IssueCache issue = issueService.requireIssue(githubRepositoryId, issueNumber, session);
        String personalAccessToken = authService.requirePersonalAccessToken(session);
        List<RemoteComment> comments = platformGatewayResolver.getGateway(PlatformType.GITHUB).getIssueComments(
            personalAccessToken,
            repository.getOwnerKey(),
            repository.getName(),
            issueNumber.toString()
        );
        comments.forEach(comment -> upsertComment(issue, comment));

        syncStateService.recordSuccess(
            SyncResourceType.COMMENT_LIST,
            issue.getExternalId(),
            "Comment cache refreshed."
        );

        return getComments(githubRepositoryId, issueNumber, session);
    }

    @Transactional
    public CommentResponse createComment(Long githubRepositoryId, Integer issueNumber, CreateCommentRequest request, HttpSession session) {
        var repository = repositoryService.requireAccessibleRepository(githubRepositoryId, session);
        IssueCache issue = issueService.requireIssue(githubRepositoryId, issueNumber, session);
        String personalAccessToken = authService.requirePersonalAccessToken(session);
        RemoteComment createdComment = platformGatewayResolver.getGateway(PlatformType.GITHUB).createComment(
            personalAccessToken,
            repository.getOwnerKey(),
            repository.getName(),
            issueNumber.toString(),
            request.body()
        );
        CommentCache comment = upsertComment(issue, createdComment);

        syncStateService.recordSuccess(
            SyncResourceType.COMMENT_LIST,
            issue.getExternalId(),
            "Comment created in cache."
        );

        return toResponse(comment);
    }

    private CommentCache upsertComment(IssueCache issue, RemoteComment commentInfo) {
        return commentCacheRepository.findByPlatformAndIssueExternalIdOrderByCreatedAtAsc(
                PlatformType.GITHUB,
                issue.getExternalId()
            ).stream()
            .filter(existing -> existing.getExternalId().equals(commentInfo.externalId()))
            .findFirst()
            .orElseGet(() -> commentCacheRepository.save(new CommentCache(
                PlatformType.GITHUB,
                commentInfo.externalId(),
                issue.getExternalId(),
                commentInfo.authorLogin(),
                commentInfo.body(),
                commentInfo.createdAt(),
                commentInfo.updatedAt(),
                commentInfo.updatedAt()
            )));
    }

    private CommentResponse toResponse(CommentCache comment) {
        return new CommentResponse(
            Long.parseLong(comment.getExternalId()),
            comment.getAuthorLogin(),
            comment.getBody(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            comment.getLastSyncedAt()
        );
    }
}
