package com.jw.github_issue_manager.platform.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jw.github_issue_manager.core.platform.PlatformGateway;
import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteComment;
import com.jw.github_issue_manager.core.remote.RemoteIssue;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.core.remote.RemoteUserProfile;

class PlatformCredentialFacadeTest {

    @Test
    void validatesCredentialWithNormalizedGitLabBaseUrl() {
        StubPlatformGateway gateway = new StubPlatformGateway();
        PlatformCredentialFacade facade = new PlatformCredentialFacade(new PlatformGatewayResolver(List.of(gateway)));

        var result = facade.validateCredential(PlatformType.GITLAB, "token", "https://gitlab.example.com");

        assertThat(gateway.baseUrl).isEqualTo("https://gitlab.example.com/api/v4");
        assertThat(result.baseUrl()).isEqualTo("https://gitlab.example.com/api/v4");
        assertThat(result.login()).isEqualTo("gitlab-user");
    }

    @Test
    void appendsApiPathWhenGitLabBaseUrlDoesNotIncludeApiPath() {
        PlatformCredentialFacade facade = new PlatformCredentialFacade(new PlatformGatewayResolver(List.of(new StubPlatformGateway())));

        String normalized = facade.resolvePlatformBaseUrl(PlatformType.GITLAB, "https://gitlab.example.com");

        assertThat(normalized).isEqualTo("https://gitlab.example.com/api/v4");
    }

    @Test
    void rejectsNonHttpsGitLabBaseUrl() {
        PlatformCredentialFacade facade = new PlatformCredentialFacade(new PlatformGatewayResolver(List.of(new StubPlatformGateway())));

        assertThatThrownBy(() -> facade.resolvePlatformBaseUrl(PlatformType.GITLAB, "http://gitlab.example.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("GitLab baseUrl must be a valid HTTPS API base URL.");
    }

    private static class StubPlatformGateway implements PlatformGateway {

        private String baseUrl;

        @Override
        public PlatformType getPlatformType() {
            return PlatformType.GITLAB;
        }

        @Override
        public RemoteUserProfile getAuthenticatedUser(String accessToken, String baseUrl) {
            this.baseUrl = baseUrl;
            return new RemoteUserProfile(PlatformType.GITLAB, "42", "gitlab-user", "GitLab User", "gitlab@example.com", "avatar");
        }

        @Override
        public List<RemoteRepository> getAccessibleRepositories(String accessToken, String baseUrl) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RemoteIssue> getRepositoryIssues(String accessToken, String baseUrl, String ownerKey, String repositoryName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RemoteIssue createIssue(String accessToken, String baseUrl, String ownerKey, String repositoryName, String title, String body) {
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RemoteComment> getIssueComments(String accessToken, String baseUrl, String ownerKey, String repositoryName, String issueKey) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RemoteComment createComment(String accessToken, String baseUrl, String ownerKey, String repositoryName, String issueKey, String body) {
            throw new UnsupportedOperationException();
        }
    }
}
