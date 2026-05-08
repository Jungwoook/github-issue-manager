package com.jw.github_issue_manager.github;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jw.github_issue_manager.core.platform.PlatformGateway;
import com.jw.github_issue_manager.core.platform.PlatformResult;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.core.remote.RemoteUserProfile;

@Component
public class GitHubPlatformGateway implements PlatformGateway {

    private final GitHubApiClient gitHubApiClient;

    public GitHubPlatformGateway(GitHubApiClient gitHubApiClient) {
        this.gitHubApiClient = gitHubApiClient;
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.GITHUB;
    }

    @Override
    public RemoteUserProfile getAuthenticatedUser(String accessToken, String baseUrl) {
        GitHubUserProfile profile = gitHubApiClient.getAuthenticatedUser(accessToken);
        return new RemoteUserProfile(
            PlatformType.GITHUB,
            profile.id().toString(),
            profile.login(),
            profile.name(),
            profile.email(),
            profile.avatarUrl()
        );
    }

    @Override
    public List<RemoteRepository> getAccessibleRepositories(String accessToken, String baseUrl) {
        return gitHubApiClient.getAccessibleRepositories(accessToken).stream()
            .map(this::toRemoteRepository)
            .toList();
    }

    @Override
    public PlatformResult<List<RemoteRepository>> getAccessibleRepositoriesWithRateLimit(String accessToken, String baseUrl) {
        GitHubApiResult<List<GitHubRepositoryInfo>> result = gitHubApiClient.getAccessibleRepositoriesWithRateLimit(accessToken);
        List<RemoteRepository> repositories = result.data().stream()
            .map(this::toRemoteRepository)
            .toList();
        return new PlatformResult<>(repositories, result.rateLimitSnapshot());
    }

    @Override
    public List<RemoteIssue> getRepositoryIssues(String accessToken, String baseUrl, String ownerKey, String repositoryName) {
        return gitHubApiClient.getRepositoryIssues(accessToken, ownerKey, repositoryName).stream()
            .map(this::toRemoteIssue)
            .toList();
    }

    @Override
    public PlatformResult<List<RemoteIssue>> getRepositoryIssuesWithRateLimit(String accessToken, String baseUrl, String ownerKey, String repositoryName) {
        GitHubApiResult<List<GitHubIssueInfo>> result = gitHubApiClient.getRepositoryIssuesWithRateLimit(accessToken, ownerKey, repositoryName);
        List<RemoteIssue> issues = result.data().stream()
            .map(this::toRemoteIssue)
            .toList();
        return new PlatformResult<>(issues, result.rateLimitSnapshot());
    }

    @Override
    public PlatformResult<RemoteIssue> getRepositoryIssueWithRateLimit(
        String accessToken,
        String baseUrl,
        String ownerKey,
        String repositoryName,
        String issueKey
    ) {
        GitHubApiResult<GitHubIssueInfo> result = gitHubApiClient.getRepositoryIssueWithRateLimit(
            accessToken,
            ownerKey,
            repositoryName,
            Integer.parseInt(issueKey)
        );
        return new PlatformResult<>(toRemoteIssue(result.data()), result.rateLimitSnapshot());
    }

    @Override
    public RemoteIssue createIssue(String accessToken, String baseUrl, String ownerKey, String repositoryName, String title, String body) {
        return toRemoteIssue(gitHubApiClient.createIssue(accessToken, ownerKey, repositoryName, title, body));
    }

    @Override
    public RemoteIssue updateIssue(
        String accessToken,
        String baseUrl,
        String ownerKey,
        String repositoryName,
        String issueKey,
        String title,
        String body,
        String state
    ) {
        return toRemoteIssue(gitHubApiClient.updateIssue(
            accessToken,
            ownerKey,
            repositoryName,
            Integer.parseInt(issueKey),
            title,
            body,
            state
        ));
    }

    @Override
    public List<RemoteComment> getIssueComments(String accessToken, String baseUrl, String ownerKey, String repositoryName, String issueKey) {
        return gitHubApiClient.getIssueComments(accessToken, ownerKey, repositoryName, Integer.parseInt(issueKey)).stream()
            .map(this::toRemoteComment)
            .toList();
    }

    @Override
    public RemoteComment createComment(String accessToken, String baseUrl, String ownerKey, String repositoryName, String issueKey, String body) {
        return toRemoteComment(gitHubApiClient.createComment(accessToken, ownerKey, repositoryName, Integer.parseInt(issueKey), body));
    }

    private RemoteRepository toRemoteRepository(GitHubRepositoryInfo repository) {
        return new RemoteRepository(
            PlatformType.GITHUB,
            repository.id().toString(),
            repository.ownerLogin(),
            repository.name(),
            repository.fullName(),
            repository.description(),
            repository.isPrivate(),
            repository.htmlUrl(),
            repository.defaultBranch(),
            repository.pushedAt()
        );
    }

    private RemoteIssue toRemoteIssue(GitHubIssueInfo issue) {
        return new RemoteIssue(
            PlatformType.GITHUB,
            issue.id().toString(),
            null,
            issue.number().toString(),
            issue.title(),
            issue.body(),
            issue.state(),
            issue.authorLogin(),
            issue.createdAt(),
            issue.updatedAt(),
            issue.closedAt()
        );
    }

    private RemoteComment toRemoteComment(GitHubCommentInfo comment) {
        return new RemoteComment(
            PlatformType.GITHUB,
            comment.id().toString(),
            null,
            comment.authorLogin(),
            comment.body(),
            comment.createdAt(),
            comment.updatedAt()
        );
    }
}
