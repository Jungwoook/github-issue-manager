package com.jw.github_issue_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.TokenAccess;
import com.jw.github_issue_manager.domain.CommentCache;
import com.jw.github_issue_manager.domain.IssueCache;
import com.jw.github_issue_manager.domain.SyncResourceType;
import com.jw.github_issue_manager.dto.comment.CommentResponse;
import com.jw.github_issue_manager.dto.comment.CreateCommentRequest;
import com.jw.github_issue_manager.repository.CommentCacheRepository;
import com.jw.github_issue_manager.repository.api.RepositoryFacade;

import jakarta.servlet.http.HttpSession;

@Service
public class CommentService {

    private final CommentCacheRepository commentCacheRepository;
    private final IssueService issueService;
    private final RepositoryFacade repositoryFacade;
    private final SyncStateService syncStateService;
    private final PlatformConnectionFacade platformConnectionFacade;
    private final PlatformGatewayResolver platformGatewayResolver;

    public CommentService(
        CommentCacheRepository commentCacheRepository,
        IssueService issueService,
        RepositoryFacade repositoryFacade,
        SyncStateService syncStateService,
        PlatformConnectionFacade platformConnectionFacade,
        PlatformGatewayResolver platformGatewayResolver
    ) {
        this.commentCacheRepository = commentCacheRepository;
        this.issueService = issueService;
        this.repositoryFacade = repositoryFacade;
        this.syncStateService = syncStateService;
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformGatewayResolver = platformGatewayResolver;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        IssueCache issue = issueService.requireIssue(platform, repositoryId, issueNumberOrKey, session);
        return commentCacheRepository.findByPlatformAndIssueExternalIdOrderByCreatedAtAsc(platform, issue.getExternalId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<CommentResponse> refreshComments(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        var repository = repositoryFacade.requireAccessibleRepository(platform, repositoryId, session);
        IssueCache issue = issueService.requireIssue(platform, repositoryId, issueNumberOrKey, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        List<RemoteComment> comments = platformGatewayResolver.getGateway(platform).getIssueComments(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.ownerKey(),
            repository.name(),
            issueNumberOrKey
        );
        comments.forEach(comment -> upsertComment(issue, comment));

        syncStateService.recordSuccess(
            SyncResourceType.COMMENT_LIST,
            platform.name() + ":" + issue.getExternalId(),
            "Comment cache refreshed."
        );

        return getComments(platform, repositoryId, issueNumberOrKey, session);
    }

    @Transactional
    public CommentResponse createComment(
        PlatformType platform,
        String repositoryId,
        String issueNumberOrKey,
        CreateCommentRequest request,
        HttpSession session
    ) {
        var repository = repositoryFacade.requireAccessibleRepository(platform, repositoryId, session);
        IssueCache issue = issueService.requireIssue(platform, repositoryId, issueNumberOrKey, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        RemoteComment createdComment = platformGatewayResolver.getGateway(platform).createComment(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.ownerKey(),
            repository.name(),
            issueNumberOrKey,
            request.body()
        );
        CommentCache comment = upsertComment(issue, createdComment);

        syncStateService.recordSuccess(
            SyncResourceType.COMMENT_LIST,
            platform.name() + ":" + issue.getExternalId(),
            "Comment created in cache."
        );

        return toResponse(comment);
    }

    private CommentCache upsertComment(IssueCache issue, RemoteComment commentInfo) {
        return commentCacheRepository.findByPlatformAndExternalId(issue.getPlatform(), commentInfo.externalId())
            .orElseGet(() -> commentCacheRepository.save(new CommentCache(
                issue.getPlatform(),
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
            comment.getPlatform(),
            comment.getExternalId(),
            comment.getAuthorLogin(),
            comment.getBody(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            comment.getLastSyncedAt()
        );
    }
}
