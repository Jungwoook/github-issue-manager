package com.jw.github_issue_manager.gitlab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.core.remote.RemoteUserProfile;

@ExtendWith(MockitoExtension.class)
class GitLabPlatformGatewayTest {

    @Mock
    private GitLabApiClient gitLabApiClient;

    @InjectMocks
    private GitLabPlatformGateway gitLabPlatformGateway;

    @Test
    void mapsAuthenticatedUserToRemoteProfile() {
        when(gitLabApiClient.getAuthenticatedUser("token", "https://gitlab.example.com/api/v4"))
            .thenReturn(new GitLabUserProfile(10L, "gitlab-user", "GitLab User", "gitlab@example.com", "avatar"));

        RemoteUserProfile profile = gitLabPlatformGateway.getAuthenticatedUser("token", "https://gitlab.example.com/api/v4");

        assertThat(profile.platform()).isEqualTo(PlatformType.GITLAB);
        assertThat(profile.externalUserId()).isEqualTo("10");
        assertThat(profile.login()).isEqualTo("gitlab-user");
        assertThat(profile.displayName()).isEqualTo("GitLab User");
    }

    @Test
    void mapsAccessibleProjectsToRemoteRepositoriesUsingAuthenticatedLoginForOwnership() {
        when(gitLabApiClient.getAuthenticatedUser("token", "https://gitlab.example.com/api/v4"))
            .thenReturn(new GitLabUserProfile(10L, "gitlab-user", "GitLab User", "gitlab@example.com", "avatar"));
        when(gitLabApiClient.getAccessibleProjects("token", "https://gitlab.example.com/api/v4"))
            .thenReturn(List.of(new GitLabProjectInfo(
                25L,
                "group/sub/project-a",
                "project-a",
                "Project A",
                true,
                "https://gitlab.com/group/sub/project-a"
            )));

        List<RemoteRepository> repositories = gitLabPlatformGateway.getAccessibleRepositories("token", "https://gitlab.example.com/api/v4");

        assertThat(repositories).hasSize(1);
        assertThat(repositories.get(0).platform()).isEqualTo(PlatformType.GITLAB);
        assertThat(repositories.get(0).externalId()).isEqualTo("25");
        assertThat(repositories.get(0).ownerKey()).isEqualTo("gitlab-user");
        assertThat(repositories.get(0).name()).isEqualTo("group/sub/project-a");
        assertThat(repositories.get(0).fullName()).isEqualTo("group/sub/project-a");
    }

    @Test
    void usesProjectPathAndIssueIidForIssueAndCommentOperations() {
        when(gitLabApiClient.getProjectIssues("token", "https://gitlab.example.com/api/v4", "group/sub/project-a"))
            .thenReturn(List.of(new GitLabIssueInfo(
                99L,
                25L,
                7L,
                "Issue title",
                "Issue body",
                "OPEN",
                "gitlab-user",
                LocalDateTime.of(2026, 4, 18, 9, 0),
                LocalDateTime.of(2026, 4, 18, 10, 0),
                null
            )));
        when(gitLabApiClient.getIssueComments("token", "https://gitlab.example.com/api/v4", "group/sub/project-a", "7"))
            .thenReturn(List.of(new GitLabCommentInfo(
                501L,
                "gitlab-user",
                "First note",
                LocalDateTime.of(2026, 4, 18, 10, 30),
                LocalDateTime.of(2026, 4, 18, 10, 30)
            )));

        List<RemoteIssue> issues = gitLabPlatformGateway.getRepositoryIssues(
            "token",
            "https://gitlab.example.com/api/v4",
            "ignored-owner",
            "group/sub/project-a"
        );
        List<RemoteComment> comments = gitLabPlatformGateway.getIssueComments(
            "token",
            "https://gitlab.example.com/api/v4",
            "ignored-owner",
            "group/sub/project-a",
            "7"
        );

        verify(gitLabApiClient).getProjectIssues("token", "https://gitlab.example.com/api/v4", "group/sub/project-a");
        verify(gitLabApiClient).getIssueComments("token", "https://gitlab.example.com/api/v4", "group/sub/project-a", "7");
        assertThat(issues.get(0).numberOrKey()).isEqualTo("7");
        assertThat(comments.get(0).externalId()).isEqualTo("501");
    }
}
