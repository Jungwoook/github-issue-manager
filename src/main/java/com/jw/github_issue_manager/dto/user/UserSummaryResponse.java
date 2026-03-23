package com.jw.github_issue_manager.dto.user;

import com.jw.github_issue_manager.domain.User;

public record UserSummaryResponse(
    Long id,
    String username,
    String displayName
) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
