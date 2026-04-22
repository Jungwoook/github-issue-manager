package com.jw.github_issue_manager.connection.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterPlatformTokenRequest(
    @NotBlank(message = "accessToken is required")
    String accessToken,
    String baseUrl
) {
}
