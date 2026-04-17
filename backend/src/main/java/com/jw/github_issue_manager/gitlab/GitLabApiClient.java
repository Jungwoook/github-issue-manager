package com.jw.github_issue_manager.gitlab;

import java.util.List;

public interface GitLabApiClient {

    GitLabUserProfile getAuthenticatedUser(String personalAccessToken);

    List<GitLabProjectInfo> getAccessibleProjects(String personalAccessToken);

    List<GitLabIssueInfo> getProjectIssues(String personalAccessToken, String projectPath);

    GitLabIssueInfo createIssue(String personalAccessToken, String projectPath, String title, String body);

    GitLabIssueInfo updateIssue(
        String personalAccessToken,
        String projectPath,
        String issueIid,
        String title,
        String body,
        String state
    );

    List<GitLabCommentInfo> getIssueComments(String personalAccessToken, String projectPath, String issueIid);

    GitLabCommentInfo createComment(String personalAccessToken, String projectPath, String issueIid, String body);
}
