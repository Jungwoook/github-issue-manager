package com.jw.github_issue_manager.comment.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.comment.internal.domain.CommentCache;
import com.jw.github_issue_manager.comment.api.dto.CommentResponse;
import com.jw.github_issue_manager.comment.internal.repository.CommentCacheRepository;

@Service
public class CommentService {

    private final CommentCacheRepository commentCacheRepository;

    public CommentService(CommentCacheRepository commentCacheRepository) {
        this.commentCacheRepository = commentCacheRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(PlatformType platform, String issueExternalId) {
        return commentCacheRepository.findByPlatformAndIssueExternalIdOrderByCreatedAtAsc(platform, issueExternalId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<CommentResponse> upsertComments(PlatformType platform, String issueExternalId, List<RemoteComment> comments) {
        comments.forEach(comment -> upsertCommentCache(platform, issueExternalId, comment));
        return getComments(platform, issueExternalId);
    }

    @Transactional
    public CommentResponse upsertComment(PlatformType platform, String issueExternalId, RemoteComment comment) {
        return toResponse(upsertCommentCache(platform, issueExternalId, comment));
    }

    private CommentCache upsertCommentCache(PlatformType platform, String issueExternalId, RemoteComment commentInfo) {
        return commentCacheRepository.findByPlatformAndExternalId(platform, commentInfo.externalId())
            .orElseGet(() -> commentCacheRepository.save(new CommentCache(
                platform,
                commentInfo.externalId(),
                issueExternalId,
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
