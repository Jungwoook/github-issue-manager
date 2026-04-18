package com.jw.github_issue_manager.gitlab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class DefaultGitLabApiClientTest {

    private MockRestServiceServer server;
    private DefaultGitLabApiClient gitLabApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        gitLabApiClient = new DefaultGitLabApiClient(
            builder.build(),
            new GitLabIntegrationProperties("https://gitlab.com/api/v4")
        );
    }

    @Test
    void createIssueUsesSingleEncodedProjectPath() {
        server.expect(requestTo("https://gitlab.example.com/api/v4/projects/group%2Fsub%2Fproject-a/issues"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("PRIVATE-TOKEN", "token"))
            .andRespond(withSuccess("""
                {
                  "id": 101,
                  "project_id": 25,
                  "iid": 7,
                  "title": "Issue title",
                  "description": "Issue body",
                  "state": "opened",
                  "author": { "username": "gitlab-user" },
                  "created_at": "2026-04-18T09:00:00Z",
                  "updated_at": "2026-04-18T10:00:00Z",
                  "closed_at": null
                }
                """, MediaType.APPLICATION_JSON));

        GitLabIssueInfo issue = gitLabApiClient.createIssue(
            "token",
            "https://gitlab.example.com/api/v4",
            "group/sub/project-a",
            "Issue title",
            "Issue body"
        );

        server.verify();
        assertThat(issue.iid()).isEqualTo(7L);
        assertThat(issue.projectId()).isEqualTo(25L);
    }
}
