package com.jw.github_issue_manager.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
    @NotBlank String body
) {
}
