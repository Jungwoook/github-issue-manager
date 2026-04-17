package com.jw.github_issue_manager.gitlab;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jw.github_issue_manager.core.platform.PlatformGateway;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.core.remote.RemoteUserProfile;

@Component
public class GitLabPlatformGateway implements PlatformGateway {

    private final GitLabApiClient gitLabApiClient;

    public GitLabPlatformGateway(GitLabApiClient gitLabApiClient) {
        this.gitLabApiClient = gitLabApiClient;
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.GITLAB;
    }

    @Override
    public RemoteUserProfile getAuthenticatedUser(String accessToken) {
        GitLabUserProfile profile = gitLabApiClient.getAuthenticatedUser(accessToken);
        return new RemoteUserProfile(
            PlatformType.GITLAB,
            profile.id().toString(),
            profile.username(),
            profile.name(),
            profile.email(),
            profile.avatarUrl()
        );
    }

    @Override
    public List<RemoteRepository> getAccessibleRepositories(String accessToken) {
        GitLabUserProfile profile = gitLabApiClient.getAuthenticatedUser(accessToken);
        return gitLabApiClient.getAccessibleProjects(accessToken).stream()
            .map(project -> toRemoteRepository(profile.username(), project))
            .toList();
    }

    @Override
    public List<RemoteIssue> getRepositoryIssues(String accessToken, String ownerKey, String repositoryName) {
        return gitLabApiClient.getProjectIssues(accessToken, repositoryName).stream()
            .map(this::toRemoteIssue)
            .toList();
    }

    @Override
    public RemoteIssue createIssue(String accessToken, String ownerKey, String repositoryName, String title, String body) {
        return toRemoteIssue(gitLabApiClient.createIssue(accessToken, repositoryName, title, body));
    }

    @Override
    public RemoteIssue updateIssue(
        String accessToken,
        String ownerKey,
        String repositoryName,
        String issueKey,
        String title,
        String body,
        String state
    ) {
        return toRemoteIssue(gitLabApiClient.updateIssue(accessToken, repositoryName, issueKey, title, body, state));
    }

    @Override
    public List<RemoteComment> getIssueComments(String accessToken, String ownerKey, String repositoryName, String issueKey) {
        return gitLabApiClient.getIssueComments(accessToken, repositoryName, issueKey).stream()
            .map(this::toRemoteComment)
            .toList();
    }

    @Override
    public RemoteComment createComment(String accessToken, String ownerKey, String repositoryName, String issueKey, String body) {
        return toRemoteComment(gitLabApiClient.createComment(accessToken, repositoryName, issueKey, body));
    }

    private RemoteRepository toRemoteRepository(String accountLogin, GitLabProjectInfo project) {
        return new RemoteRepository(
            PlatformType.GITLAB,
            project.id().toString(),
            accountLogin,
            project.pathWithNamespace(),
            project.pathWithNamespace(),
            project.description(),
            project.isPrivate(),
            project.webUrl(),
            project.defaultBranch() == null ? "main" : project.defaultBranch(),
            project.lastActivityAt()
        );
    }

    private RemoteIssue toRemoteIssue(GitLabIssueInfo issue) {
        return new RemoteIssue(
            PlatformType.GITLAB,
            issue.id().toString(),
            issue.projectId() == null ? null : issue.projectId().toString(),
            issue.iid().toString(),
            issue.title(),
            issue.body(),
            issue.state(),
            issue.authorLogin(),
            issue.createdAt(),
            issue.updatedAt(),
            issue.closedAt()
        );
    }

    private RemoteComment toRemoteComment(GitLabCommentInfo comment) {
        return new RemoteComment(
            PlatformType.GITLAB,
            comment.id().toString(),
            null,
            comment.authorLogin(),
            comment.body(),
            comment.createdAt(),
            comment.updatedAt()
        );
    }
}
