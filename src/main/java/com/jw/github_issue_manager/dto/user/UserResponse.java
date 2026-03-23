package com.jw.github_issue_manager.dto.user;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.domain.User;
import com.jw.github_issue_manager.domain.UserRole;

public record UserResponse(
    Long id,
    String username,
    String displayName,
    String email,
    UserRole role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
