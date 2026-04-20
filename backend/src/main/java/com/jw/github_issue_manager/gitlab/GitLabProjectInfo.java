package com.jw.github_issue_manager.gitlab;

import java.time.LocalDateTime;

public record GitLabProjectInfo(
    Long id,
    String pathWithNamespace,
    String name,
    String description,
    boolean isPrivate,
    String webUrl
) {
}
