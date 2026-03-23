package com.jw.github_issue_manager.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
    @NotBlank(message = "content must not be blank")
    String content,

    @NotNull(message = "authorId must not be null")
    Long authorId
) {
}
