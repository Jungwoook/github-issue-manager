package com.jw.github_issue_manager.github;

import java.util.List;

public interface GitHubApiClient {

    GitHubUserProfile getAuthenticatedUser(String personalAccessToken);

    List<GitHubRepositoryInfo> getAccessibleRepositories(String personalAccessToken);

    default GitHubApiResult<List<GitHubRepositoryInfo>> getAccessibleRepositoriesWithRateLimit(String personalAccessToken) {
        return new GitHubApiResult<>(getAccessibleRepositories(personalAccessToken), null);
    }

    List<GitHubIssueInfo> getRepositoryIssues(String personalAccessToken, String owner, String repositoryName);

    default GitHubApiResult<List<GitHubIssueInfo>> getRepositoryIssuesWithRateLimit(String personalAccessToken, String owner, String repositoryName) {
        return new GitHubApiResult<>(getRepositoryIssues(personalAccessToken, owner, repositoryName), null);
    }

    GitHubIssueInfo createIssue(String personalAccessToken, String owner, String repositoryName, String title, String body);

    GitHubIssueInfo updateIssue(String personalAccessToken, String owner, String repositoryName, int issueNumber, String title, String body, String state);

    List<GitHubCommentInfo> getIssueComments(String personalAccessToken, String owner, String repositoryName, int issueNumber);

    GitHubCommentInfo createComment(String personalAccessToken, String owner, String repositoryName, int issueNumber, String body);
}
