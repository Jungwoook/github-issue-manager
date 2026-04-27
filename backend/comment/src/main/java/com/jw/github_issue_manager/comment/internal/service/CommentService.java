package com.jw.github_issue_manager.comment.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.comment.internal.domain.CommentCache;
import com.jw.github_issue_manager.domain.SyncResourceType;
import com.jw.github_issue_manager.comment.api.dto.CommentResponse;
import com.jw.github_issue_manager.comment.api.dto.CreateCommentRequest;
import com.jw.github_issue_manager.issue.api.IssueAccess;
import com.jw.github_issue_manager.issue.api.IssueFacade;
import com.jw.github_issue_manager.comment.internal.repository.CommentCacheRepository;
import com.jw.github_issue_manager.platform.api.PlatformRemoteFacade;
import com.jw.github_issue_manager.repository.api.RepositoryFacade;
import com.jw.github_issue_manager.service.SyncStateService;

import jakarta.servlet.http.HttpSession;

@Service
public class CommentService {

    private final CommentCacheRepository commentCacheRepository;
    private final IssueFacade issueFacade;
    private final RepositoryFacade repositoryFacade;
    private final SyncStateService syncStateService;
    private final PlatformRemoteFacade platformRemoteFacade;

    public CommentService(
        CommentCacheRepository commentCacheRepository,
        IssueFacade issueFacade,
        RepositoryFacade repositoryFacade,
        SyncStateService syncStateService,
        PlatformRemoteFacade platformRemoteFacade
    ) {
        this.commentCacheRepository = commentCacheRepository;
        this.issueFacade = issueFacade;
        this.repositoryFacade = repositoryFacade;
        this.syncStateService = syncStateService;
        this.platformRemoteFacade = platformRemoteFacade;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        IssueAccess issue = issueFacade.requireIssue(platform, repositoryId, issueNumberOrKey, session);
        return commentCacheRepository.findByPlatformAndIssueExternalIdOrderByCreatedAtAsc(platform, issue.externalId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<CommentResponse> refreshComments(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
        var repository = repositoryFacade.requireAccessibleRepository(platform, repositoryId, session);
        IssueAccess issue = issueFacade.requireIssue(platform, repositoryId, issueNumberOrKey, session);
        List<RemoteComment> comments = platformRemoteFacade.getIssueComments(
            platform,
            session,
            repository.ownerKey(),
            repository.name(),
            issueNumberOrKey
        );
        comments.forEach(comment -> upsertComment(issue, comment));

        syncStateService.recordSuccess(
            SyncResourceType.COMMENT_LIST,
            platform.name() + ":" + issue.externalId(),
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
        IssueAccess issue = issueFacade.requireIssue(platform, repositoryId, issueNumberOrKey, session);
        RemoteComment createdComment = platformRemoteFacade.createComment(
            platform,
            session,
            repository.ownerKey(),
            repository.name(),
            issueNumberOrKey,
            request.body()
        );
        CommentCache comment = upsertComment(issue, createdComment);

        syncStateService.recordSuccess(
            SyncResourceType.COMMENT_LIST,
            platform.name() + ":" + issue.externalId(),
            "Comment created in cache."
        );

        return toResponse(comment);
    }

    private CommentCache upsertComment(IssueAccess issue, RemoteComment commentInfo) {
        return commentCacheRepository.findByPlatformAndExternalId(issue.platform(), commentInfo.externalId())
            .orElseGet(() -> commentCacheRepository.save(new CommentCache(
                issue.platform(),
                commentInfo.externalId(),
                issue.externalId(),
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
