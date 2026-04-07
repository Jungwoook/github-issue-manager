package com.jw.github_issue_manager.github;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.github")
public record GitHubIntegrationProperties(
    String apiBaseUrl,
    String patEncryptionKey
) {
}
