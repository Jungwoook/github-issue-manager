package com.jw.github_issue_manager.gitlab;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class DefaultGitLabApiClient implements GitLabApiClient {

    private final RestClient restClient;
    private final GitLabIntegrationProperties properties;

    public DefaultGitLabApiClient(RestClient restClient, GitLabIntegrationProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public GitLabUserProfile getAuthenticatedUser(String personalAccessToken, String apiBaseUrl) {
        GitLabUserResponse response = apiRequest(apiBaseUrl, "/user", personalAccessToken, GitLabUserResponse.class);
        if (response == null) {
            throw new GitLabApiException("GitLab user response was empty.");
        }
        return new GitLabUserProfile(
            response.id(),
            response.username(),
            response.name() == null || response.name().isBlank() ? response.username() : response.name(),
            response.email(),
            response.avatarUrl()
        );
    }

    @Override
    public List<GitLabProjectInfo> getAccessibleProjects(String personalAccessToken, String apiBaseUrl) {
        String uri = UriComponentsBuilder.fromUriString(resolveApiBaseUrl(apiBaseUrl) + "/projects")
            .queryParam("membership", "true")
            .queryParam("min_access_level", "10")
            .queryParam("simple", "true")
            .queryParam("per_page", "100")
            .toUriString();
        GitLabProjectResponse[] response = apiRequestAbsolute(uri, personalAccessToken, GitLabProjectResponse[].class);
        if (response == null) {
            return List.of();
        }
        return List.of(response).stream()
            .map(project -> new GitLabProjectInfo(
                project.id(),
                project.pathWithNamespace(),
                project.name(),
            project.description(),
            "private".equalsIgnoreCase(project.visibility()),
            project.webUrl()
        ))
            .toList();
    }

    @Override
    public List<GitLabIssueInfo> getProjectIssues(String personalAccessToken, String apiBaseUrl, String projectPath) {
        String uri = UriComponentsBuilder.fromUri(projectApiUri(apiBaseUrl, projectPath, "/issues"))
            .queryParam("state", "all")
            .queryParam("per_page", "100")
            .build(true)
            .toUriString();
        GitLabIssueResponse[] response = apiRequestAbsolute(uri, personalAccessToken, GitLabIssueResponse[].class);
        if (response == null) {
            return List.of();
        }
        return List.of(response).stream()
            .map(this::toIssueInfo)
            .toList();
    }

    @Override
    public GitLabIssueInfo createIssue(String personalAccessToken, String apiBaseUrl, String projectPath, String title, String body) {
        GitLabIssueResponse response = restClient.post()
            .uri(projectApiUri(apiBaseUrl, projectPath, "/issues"))
            .header("PRIVATE-TOKEN", personalAccessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("title", title, "description", body == null ? "" : body))
            .retrieve()
            .body(GitLabIssueResponse.class);
        return toIssueInfo(response);
    }

    @Override
    public GitLabIssueInfo updateIssue(
        String personalAccessToken,
        String apiBaseUrl,
        String projectPath,
        String issueIid,
        String title,
        String body,
        String state
    ) {
        Map<String, String> payload = new LinkedHashMap<>();
        if (title != null) {
            payload.put("title", title);
        }
        if (body != null) {
            payload.put("description", body);
        }
        if (state != null && !state.isBlank()) {
            payload.put("state_event", normalizeStateEvent(state));
        }

        GitLabIssueResponse response = restClient.put()
            .uri(projectApiUri(apiBaseUrl, projectPath, "/issues/" + issueIid))
            .header("PRIVATE-TOKEN", personalAccessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .body(GitLabIssueResponse.class);
        return toIssueInfo(response);
    }

    @Override
    public List<GitLabCommentInfo> getIssueComments(String personalAccessToken, String apiBaseUrl, String projectPath, String issueIid) {
        String uri = UriComponentsBuilder.fromUri(projectApiUri(apiBaseUrl, projectPath, "/issues/" + issueIid + "/notes"))
            .build(true)
            .toUriString();
        GitLabNoteResponse[] response = apiRequestAbsolute(
            uri,
            personalAccessToken,
            GitLabNoteResponse[].class
        );
        if (response == null) {
            return List.of();
        }
        return List.of(response).stream()
            .filter(note -> !note.system())
            .map(this::toCommentInfo)
            .toList();
    }

    @Override
    public GitLabCommentInfo createComment(String personalAccessToken, String apiBaseUrl, String projectPath, String issueIid, String body) {
        GitLabNoteResponse response = restClient.post()
            .uri(projectApiUri(apiBaseUrl, projectPath, "/issues/" + issueIid + "/notes"))
            .header("PRIVATE-TOKEN", personalAccessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("body", body))
            .retrieve()
            .body(GitLabNoteResponse.class);
        return toCommentInfo(response);
    }

    private String projectApiPath(String apiBaseUrl, String projectPath) {
        return resolveApiBaseUrl(apiBaseUrl) + "/projects/" + encodeProjectPath(projectPath);
    }

    private URI projectApiUri(String apiBaseUrl, String projectPath, String suffix) {
        return URI.create(projectApiPath(apiBaseUrl, projectPath) + suffix);
    }

    private String encodeProjectPath(String projectPath) {
        return UriUtils.encodePathSegment(projectPath, StandardCharsets.UTF_8);
    }

    private String normalizeStateEvent(String state) {
        return switch (state.toUpperCase()) {
            case "CLOSED", "CLOSE", "CLOSED_STATE" -> "close";
            case "OPEN", "OPENED", "REOPEN" -> "reopen";
            default -> state.toLowerCase();
        };
    }

    private String resolveApiBaseUrl(String apiBaseUrl) {
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            return properties.apiBaseUrl();
        }
        return apiBaseUrl;
    }

    private <T> T apiRequest(String apiBaseUrl, String path, String token, Class<T> responseType) {
        return apiRequestAbsolute(resolveApiBaseUrl(apiBaseUrl) + path, token, responseType);
    }

    private <T> T apiRequestAbsolute(String uri, String token, Class<T> responseType) {
        return restClient.get()
            .uri(uri)
            .header("PRIVATE-TOKEN", token)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .body(responseType);
    }

    private GitLabIssueInfo toIssueInfo(GitLabIssueResponse issue) {
        if (issue == null) {
            throw new GitLabApiException("GitLab issue response was empty.");
        }
        return new GitLabIssueInfo(
            issue.id(),
            issue.projectId(),
            issue.iid(),
            issue.title(),
            issue.description(),
            normalizeState(issue.state()),
            issue.author() == null ? "unknown" : issue.author().username(),
            toLocalDateTime(issue.createdAt()),
            toLocalDateTime(issue.updatedAt()),
            toLocalDateTime(issue.closedAt())
        );
    }

    private GitLabCommentInfo toCommentInfo(GitLabNoteResponse note) {
        if (note == null) {
            throw new GitLabApiException("GitLab note response was empty.");
        }
        return new GitLabCommentInfo(
            note.id(),
            note.author() == null ? "unknown" : note.author().username(),
            note.body(),
            toLocalDateTime(note.createdAt()),
            toLocalDateTime(note.updatedAt())
        );
    }

    private String normalizeState(String state) {
        if (state == null || state.isBlank()) {
            return "OPEN";
        }
        return switch (state.toLowerCase()) {
            case "opened", "open" -> "OPEN";
            case "closed", "close" -> "CLOSED";
            default -> state.toUpperCase();
        };
    }

    private LocalDateTime toLocalDateTime(String value) {
        return value == null ? null : OffsetDateTime.parse(value).toLocalDateTime();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitLabUserResponse(
        Long id,
        String username,
        String name,
        String email,
        @JsonProperty("avatar_url") String avatarUrl
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitLabProjectResponse(
        Long id,
        String name,
        @JsonProperty("path_with_namespace") String pathWithNamespace,
        String description,
        String visibility,
        @JsonProperty("web_url") String webUrl
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitLabIssueResponse(
        Long id,
        @JsonProperty("project_id") Long projectId,
        Long iid,
        String title,
        String description,
        String state,
        GitLabAuthorResponse author,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("closed_at") String closedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitLabNoteResponse(
        Long id,
        String body,
        boolean system,
        GitLabAuthorResponse author,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitLabAuthorResponse(String username) {
    }
}
