package com.jw.github_issue_manager.gitlab;

import java.util.List;

public interface GitLabApiClient {

    GitLabUserProfile getAuthenticatedUser(String personalAccessToken, String apiBaseUrl);

    List<GitLabProjectInfo> getAccessibleProjects(String personalAccessToken, String apiBaseUrl);

    List<GitLabIssueInfo> getProjectIssues(String personalAccessToken, String apiBaseUrl, String projectPath);

    GitLabIssueInfo createIssue(String personalAccessToken, String apiBaseUrl, String projectPath, String title, String body);

    GitLabIssueInfo updateIssue(
        String personalAccessToken,
        String apiBaseUrl,
        String projectPath,
        String issueIid,
        String title,
        String body,
        String state
    );

    List<GitLabCommentInfo> getIssueComments(String personalAccessToken, String apiBaseUrl, String projectPath, String issueIid);

    GitLabCommentInfo createComment(String personalAccessToken, String apiBaseUrl, String projectPath, String issueIid, String body);
}
