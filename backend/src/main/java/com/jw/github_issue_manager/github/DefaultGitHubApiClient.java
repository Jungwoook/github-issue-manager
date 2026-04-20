package com.jw.github_issue_manager.github;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class DefaultGitHubApiClient implements GitHubApiClient {

    private final RestClient restClient;
    private final GitHubIntegrationProperties properties;

    public DefaultGitHubApiClient(RestClient restClient, GitHubIntegrationProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public GitHubUserProfile getAuthenticatedUser(String personalAccessToken) {
        GitHubUserResponse response = apiRequest("/user", personalAccessToken, GitHubUserResponse.class);
        return new GitHubUserProfile(
            response.id(),
            response.login(),
            response.name() == null || response.name().isBlank() ? response.login() : response.name(),
            response.email(),
            response.avatarUrl()
        );
    }

    @Override
    public List<GitHubRepositoryInfo> getAccessibleRepositories(String personalAccessToken) {
        String uri = UriComponentsBuilder.fromUriString(properties.apiBaseUrl() + "/user/repos")
            .queryParam("visibility", "all")
            .queryParam("affiliation", "owner,collaborator,organization_member")
            .queryParam("per_page", "100")
            .toUriString();
        RepositoryResponse[] response = apiRequestAbsolute(uri, personalAccessToken, RepositoryResponse[].class);
        if (response == null) {
            return List.of();
        }
        return List.of(response).stream()
            .map(repository -> new GitHubRepositoryInfo(
                repository.id(),
                repository.owner().login(),
                repository.name(),
                repository.fullName(),
            repository.description(),
            repository.isPrivate(),
            repository.htmlUrl()
        ))
            .toList();
    }

    @Override
    public List<GitHubIssueInfo> getRepositoryIssues(String personalAccessToken, String owner, String repositoryName) {
        String uri = UriComponentsBuilder.fromUriString(properties.apiBaseUrl() + "/repos/" + owner + "/" + repositoryName + "/issues")
            .queryParam("state", "all")
            .toUriString();
        GitHubIssueResponse[] response = apiRequestAbsolute(uri, personalAccessToken, GitHubIssueResponse[].class);
        if (response == null) {
            return List.of();
        }
        return List.of(response).stream()
            .filter(issue -> issue.pullRequest() == null)
            .map(this::toIssueInfo)
            .toList();
    }

    @Override
    public GitHubIssueInfo createIssue(String personalAccessToken, String owner, String repositoryName, String title, String body) {
        GitHubIssueResponse response = restClient.post()
            .uri(properties.apiBaseUrl() + "/repos/" + owner + "/" + repositoryName + "/issues")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + personalAccessToken)
            .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("title", title, "body", body == null ? "" : body))
            .retrieve()
            .body(GitHubIssueResponse.class);
        return toIssueInfo(response);
    }

    @Override
    public GitHubIssueInfo updateIssue(String personalAccessToken, String owner, String repositoryName, int issueNumber, String title, String body, String state) {
        Map<String, String> payload = new java.util.LinkedHashMap<>();
        if (title != null) {
            payload.put("title", title);
        }
        if (body != null) {
            payload.put("body", body);
        }
        if (state != null) {
            payload.put("state", state.toLowerCase());
        }

        GitHubIssueResponse response = restClient.patch()
            .uri(properties.apiBaseUrl() + "/repos/" + owner + "/" + repositoryName + "/issues/" + issueNumber)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + personalAccessToken)
            .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .body(GitHubIssueResponse.class);
        return toIssueInfo(response);
    }

    @Override
    public List<GitHubCommentInfo> getIssueComments(String personalAccessToken, String owner, String repositoryName, int issueNumber) {
        GitHubCommentResponse[] response = apiRequest(
            "/repos/" + owner + "/" + repositoryName + "/issues/" + issueNumber + "/comments",
            personalAccessToken,
            GitHubCommentResponse[].class
        );
        if (response == null) {
            return List.of();
        }
        return List.of(response).stream()
            .map(this::toCommentInfo)
            .toList();
    }

    @Override
    public GitHubCommentInfo createComment(String personalAccessToken, String owner, String repositoryName, int issueNumber, String body) {
        GitHubCommentResponse response = restClient.post()
            .uri(properties.apiBaseUrl() + "/repos/" + owner + "/" + repositoryName + "/issues/" + issueNumber + "/comments")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + personalAccessToken)
            .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("body", body))
            .retrieve()
            .body(GitHubCommentResponse.class);
        return toCommentInfo(response);
    }

    private <T> T apiRequest(String path, String token, Class<T> responseType) {
        return apiRequestAbsolute(properties.apiBaseUrl() + path, token, responseType);
    }

    private <T> T apiRequestAbsolute(String uri, String token, Class<T> responseType) {
        return restClient.get()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .retrieve()
            .body(responseType);
    }

    private GitHubIssueInfo toIssueInfo(GitHubIssueResponse issue) {
        if (issue == null) {
            throw new GitHubApiException("GitHub issue response was empty.");
        }
        return new GitHubIssueInfo(
            issue.id(),
            issue.number(),
            issue.title(),
            issue.body(),
            issue.state() == null ? "OPEN" : issue.state().toUpperCase(),
            issue.user() == null ? "unknown" : issue.user().login(),
            toLocalDateTime(issue.createdAt()),
            toLocalDateTime(issue.updatedAt()),
            toLocalDateTime(issue.closedAt())
        );
    }

    private GitHubCommentInfo toCommentInfo(GitHubCommentResponse comment) {
        if (comment == null) {
            throw new GitHubApiException("GitHub comment response was empty.");
        }
        return new GitHubCommentInfo(
            comment.id(),
            comment.user() == null ? "unknown" : comment.user().login(),
            comment.body(),
            toLocalDateTime(comment.createdAt()),
            toLocalDateTime(comment.updatedAt())
        );
    }

    private LocalDateTime toLocalDateTime(String value) {
        return value == null ? null : OffsetDateTime.parse(value).toLocalDateTime();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubUserResponse(Long id, String login, String name, String email, @JsonProperty("avatar_url") String avatarUrl) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RepositoryResponse(
        Long id,
        String name,
        @JsonProperty("full_name") String fullName,
        String description,
        @JsonProperty("private") boolean isPrivate,
        @JsonProperty("html_url") String htmlUrl,
        OwnerResponse owner
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OwnerResponse(String login) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubIssueResponse(
        Long id,
        Integer number,
        String title,
        String body,
        String state,
        UserResponse user,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("closed_at") String closedAt,
        @JsonProperty("pull_request") Object pullRequest
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubCommentResponse(
        Long id,
        String body,
        UserResponse user,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record UserResponse(String login) {
    }
}
