package com.jw.github_issue_manager.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RegisterGitHubTokenRequest(
    @NotBlank String accessToken
) {
}
