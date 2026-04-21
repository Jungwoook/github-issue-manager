package com.jw.github_issue_manager.dto.health;

import java.time.LocalDateTime;

public record HealthCheckResponse(
    String status,
    String application,
    LocalDateTime timestamp
) {
}
