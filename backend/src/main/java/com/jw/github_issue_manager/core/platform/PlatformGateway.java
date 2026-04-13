package com.jw.github_issue_manager.core.platform;

import java.util.List;

import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.core.remote.RemoteUserProfile;

public interface PlatformGateway {

    PlatformType getPlatformType();

    RemoteUserProfile getAuthenticatedUser(String accessToken);

    List<RemoteRepository> getAccessibleRepositories(String accessToken);

    List<RemoteIssue> getRepositoryIssues(String accessToken, String ownerKey, String repositoryName);

    RemoteIssue createIssue(String accessToken, String ownerKey, String repositoryName, String title, String body);

    RemoteIssue updateIssue(
        String accessToken,
        String ownerKey,
        String repositoryName,
        String issueKey,
        String title,
        String body,
        String state
    );

    List<RemoteComment> getIssueComments(String accessToken, String ownerKey, String repositoryName, String issueKey);

    RemoteComment createComment(String accessToken, String ownerKey, String repositoryName, String issueKey, String body);
}
