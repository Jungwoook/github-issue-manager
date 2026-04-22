package com.jw.github_issue_manager.shared.api.dto;

import java.time.LocalDateTime;

public record SyncStateResponse(
    String resourceType,
    String resourceKey,
    LocalDateTime lastSyncedAt,
    String lastSyncStatus,
    String lastSyncMessage
) {
}
