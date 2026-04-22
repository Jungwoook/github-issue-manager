package com.jw.github_issue_manager.comment.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
    @NotBlank String body
) {
}
