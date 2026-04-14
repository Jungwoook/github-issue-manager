package com.jw.github_issue_manager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jw.github_issue_manager.github.GitHubApiClient;
import com.jw.github_issue_manager.github.GitHubCommentInfo;
import com.jw.github_issue_manager.github.GitHubIssueInfo;
import com.jw.github_issue_manager.github.GitHubRepositoryInfo;
import com.jw.github_issue_manager.github.GitHubUserProfile;

@SpringBootTest(properties = {
    "app.github.pat-encryption-key=test-pat-key"
})
@AutoConfigureMockMvc
class ApiFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void githubCacheApiFlowWorks() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/platforms/github/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "accessToken": "pat-token"
                    }
                    """)
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.platform").value("GITHUB"))
            .andExpect(jsonPath("$.accountLogin").value("tester"));

        mockMvc.perform(get("/api/platforms/github/token/status").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.connected").value(true))
            .andExpect(jsonPath("$.platform").value("GITHUB"))
            .andExpect(jsonPath("$.accountLogin").value("tester"));

        mockMvc.perform(get("/api/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.displayName").value("Tester"))
            .andExpect(jsonPath("$.platform").value("GITHUB"));

        MvcResult repositoryRefresh = mockMvc.perform(post("/api/platforms/github/repositories/refresh").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].platform").value("GITHUB"))
            .andExpect(jsonPath("$[0].ownerKey").value("tester"))
            .andReturn();

        String repositoryId = JsonTestUtils.readString(repositoryRefresh.getResponse().getContentAsString(), "$[0].repositoryId");

        mockMvc.perform(get("/api/platforms/github/repositories/{repositoryId}", repositoryId).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.repositoryId").value(repositoryId));

        mockMvc.perform(post("/api/platforms/github/repositories/{repositoryId}/issues/refresh", repositoryId).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].numberOrKey").value("2"));

        mockMvc.perform(post("/api/platforms/github/repositories/{repositoryId}/issues", repositoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "New issue",
                      "body": "Created from cache"
                    }
                    """)
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("New issue"))
            .andExpect(jsonPath("$.state").value("OPEN"));

        mockMvc.perform(get("/api/platforms/github/repositories/{repositoryId}/issues", repositoryId)
                .param("keyword", "New")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("New issue"));

        mockMvc.perform(patch("/api/platforms/github/repositories/{repositoryId}/issues/{issueNumber}", repositoryId, 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "state": "closed"
                    }
                    """)
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("CLOSED"));

        mockMvc.perform(post("/api/platforms/github/repositories/{repositoryId}/issues/{issueNumber}/comments/refresh", repositoryId, 3)
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].authorLogin").value("tester"));

        mockMvc.perform(post("/api/platforms/github/repositories/{repositoryId}/issues/{issueNumber}/comments", repositoryId, 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "body": "Confirmed."
                    }
                    """)
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.body").value("Confirmed."));

        mockMvc.perform(get("/api/platforms/github/repositories/{repositoryId}/issues/{issueNumber}/sync-state", repositoryId, 3).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resourceType").value("ISSUE"));

        mockMvc.perform(delete("/api/platforms/github/repositories/{repositoryId}/issues/{issueNumber}", repositoryId, 3).session(session))
            .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/platforms/github/token").session(session))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/me").session(session))
            .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    static class FakeGitHubConfiguration {

        @Bean
        @Primary
        GitHubApiClient gitHubApiClient() {
            return new FakeGitHubApiClient();
        }

    }

    static class FakeGitHubApiClient implements GitHubApiClient {

        private final Map<Integer, GitHubIssueInfo> issues = new ConcurrentHashMap<>();
        private final Map<Integer, List<GitHubCommentInfo>> comments = new ConcurrentHashMap<>();

        FakeGitHubApiClient() {
            LocalDateTime now = LocalDateTime.now();
            issues.put(1, new GitHubIssueInfo(10101L, 1, "Initial setup check", "Validate cache behavior before GitHub App integration.", "OPEN", "tester", now.minusDays(2), now.minusDays(1), null));
            issues.put(2, new GitHubIssueInfo(10102L, 2, "Issue list refresh", "Users should be able to refresh repository issues manually.", "OPEN", "tester", now.minusDays(1), now.minusHours(6), null));
            comments.put(3, new ArrayList<>(List.of(
                new GitHubCommentInfo(30101L, "tester", "Sample comment created during refresh.", now.minusHours(3), now.minusHours(3))
            )));
        }

        @Override
        public GitHubUserProfile getAuthenticatedUser(String userAccessToken) {
            return new GitHubUserProfile(9001L, "tester", "Tester", "tester@example.com", "https://avatars.githubusercontent.com/u/9001");
        }

        @Override
        public List<GitHubRepositoryInfo> getAccessibleRepositories(String installationAccessToken) {
            return List.of(new GitHubRepositoryInfo(
                101L,
                "tester",
                "github-issue-manager",
                "tester/github-issue-manager",
                "GitHub issue manager",
                false,
                "https://github.com/tester/github-issue-manager",
                "main",
                LocalDateTime.now().minusHours(1)
            ));
        }

        @Override
        public List<GitHubIssueInfo> getRepositoryIssues(String installationAccessToken, String owner, String repositoryName) {
            return issues.values().stream()
                .sorted((left, right) -> Integer.compare(left.number(), right.number()))
                .toList();
        }

        @Override
        public GitHubIssueInfo createIssue(String installationAccessToken, String owner, String repositoryName, String title, String body) {
            int nextNumber = issues.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            GitHubIssueInfo issue = new GitHubIssueInfo(10100L + nextNumber, nextNumber, title, body, "OPEN", "tester", LocalDateTime.now(), LocalDateTime.now(), null);
            issues.put(nextNumber, issue);
            return issue;
        }

        @Override
        public GitHubIssueInfo updateIssue(String installationAccessToken, String owner, String repositoryName, int issueNumber, String title, String body, String state) {
            GitHubIssueInfo current = issues.get(issueNumber);
            GitHubIssueInfo updated = new GitHubIssueInfo(
                current.id(),
                current.number(),
                title == null ? current.title() : title,
                body == null ? current.body() : body,
                state == null ? current.state() : state.toUpperCase(),
                current.authorLogin(),
                current.createdAt(),
                LocalDateTime.now(),
                state != null && "CLOSED".equalsIgnoreCase(state) ? LocalDateTime.now() : current.closedAt()
            );
            issues.put(issueNumber, updated);
            return updated;
        }

        @Override
        public List<GitHubCommentInfo> getIssueComments(String installationAccessToken, String owner, String repositoryName, int issueNumber) {
            return comments.getOrDefault(issueNumber, List.of());
        }

        @Override
        public GitHubCommentInfo createComment(String installationAccessToken, String owner, String repositoryName, int issueNumber, String body) {
            List<GitHubCommentInfo> issueComments = comments.computeIfAbsent(issueNumber, key -> new ArrayList<>());
            long nextId = issueComments.stream().mapToLong(GitHubCommentInfo::id).max().orElse(30100L) + 1;
            GitHubCommentInfo comment = new GitHubCommentInfo(nextId, "tester", body, LocalDateTime.now(), LocalDateTime.now());
            issueComments.add(comment);
            return comment;
        }
    }
}
