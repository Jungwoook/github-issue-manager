package com.jw.github_issue_manager.application.comment;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.application.issue.IssueApplicationFacade;
import com.jw.github_issue_manager.application.sync.SyncResourceType;
import com.jw.github_issue_manager.application.sync.SyncStateService;
import com.jw.github_issue_manager.comment.api.CommentFacade;
import com.jw.github_issue_manager.comment.api.dto.CommentResponse;
import com.jw.github_issue_manager.comment.api.dto.CreateCommentRequest;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.TokenAccess;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.issue.api.IssueAccess;
import com.jw.github_issue_manager.repository.api.RepositoryAccess;
import com.jw.github_issue_manager.repository.api.RepositoryFacade;

import jakarta.servlet.http.HttpSession;

@Service
public class CommentApplicationFacade {

    private final CommentFacade commentFacade;
    private final IssueApplicationFacade issueApplicationFacade;
    private final RepositoryFacade repositoryFacade;
    private final PlatformConnectionFacade platformConnectionFacade;
    private final PlatformGatewayResolver platformGatewayResolver;
    private final SyncStateService syncStateService;

    public CommentApplicationFacade(
        CommentFacade commentFacade,
        IssueApplicationFacade issueApplicationFacade,
        RepositoryFacade repositoryFacade,
        PlatformConnectionFacade platformConnectionFacade,
        PlatformGatewayResolver platformGatewayResolver,
        SyncStateService syncStateService
    ) {
        this.commentFacade = commentFacade;
        this.issueApplicationFacade = issueApplicationFacade;
        this.repositoryFacade = repositoryFacade;
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformGatewayResolver = platformGatewayResolver;
        this.syncStateService = syncStateService;
    }

    public List<CommentResponse> getComments(
        String platform,
        String repositoryId,
        String issueNumberOrKey,
        HttpSession session
    ) {
        PlatformType platformType = PlatformType.from(platform);
        IssueAccess issue = issueApplicationFacade.requireIssue(platformType, repositoryId, issueNumberOrKey, session);
        return commentFacade.getComments(platformType, issue.externalId());
    }

    public List<CommentResponse> refreshComments(
        String platform,
        String repositoryId,
        String issueNumberOrKey,
        HttpSession session
    ) {
        PlatformType platformType = PlatformType.from(platform);
        RepositoryAccess repository = requireRepository(platformType, repositoryId, session);
        IssueAccess issue = issueApplicationFacade.requireIssue(platformType, repositoryId, issueNumberOrKey, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platformType, session);
        var comments = platformGatewayResolver.getGateway(platformType).getIssueComments(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.ownerKey(),
            repository.name(),
            issueNumberOrKey
        );
        List<CommentResponse> responses = commentFacade.upsertComments(platformType, issue.externalId(), comments);

        syncStateService.recordSuccess(
            SyncResourceType.COMMENT_LIST,
            platformType.name() + ":" + issue.externalId(),
            "Comment cache refreshed."
        );

        return responses;
    }

    public CommentResponse createComment(
        String platform,
        String repositoryId,
        String issueNumberOrKey,
        CreateCommentRequest request,
        HttpSession session
    ) {
        PlatformType platformType = PlatformType.from(platform);
        RepositoryAccess repository = requireRepository(platformType, repositoryId, session);
        IssueAccess issue = issueApplicationFacade.requireIssue(platformType, repositoryId, issueNumberOrKey, session);
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platformType, session);
        var createdComment = platformGatewayResolver.getGateway(platformType).createComment(
            tokenAccess.accessToken(),
            tokenAccess.baseUrl(),
            repository.ownerKey(),
            repository.name(),
            issueNumberOrKey,
            request.body()
        );
        CommentResponse response = commentFacade.upsertComment(platformType, issue.externalId(), createdComment);

        syncStateService.recordSuccess(
            SyncResourceType.COMMENT_LIST,
            platformType.name() + ":" + issue.externalId(),
            "Comment created in cache."
        );

        return response;
    }

    private RepositoryAccess requireRepository(PlatformType platform, String repositoryId, HttpSession session) {
        var connection = platformConnectionFacade.requireCurrentConnection(platform, session);
        return repositoryFacade.requireAccessibleRepository(platform, repositoryId, connection.accountLogin());
    }
}
