package com.jw.github_issue_manager.gitlab;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gitlab")
public record GitLabIntegrationProperties(
    String apiBaseUrl
) {
}
