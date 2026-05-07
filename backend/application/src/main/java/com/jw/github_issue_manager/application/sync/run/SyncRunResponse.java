package com.jw.github_issue_manager.application.sync.run;

import java.time.LocalDateTime;

public record SyncRunResponse(
    Long id,
    String platform,
    String resourceType,
    String resourceKey,
    String status,
    String triggerType,
    LocalDateTime startedAt,
    LocalDateTime finishedAt,
    int createdCount,
    int updatedCount,
    int skippedCount,
    int failedCount,
    String failureMessage
) {
}
