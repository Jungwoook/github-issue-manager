package com.jw.github_issue_manager.dto.label;

import com.jw.github_issue_manager.domain.Label;

public record LabelResponse(
    Long id,
    Long repositoryId,
    String name,
    String color
) {

    public static LabelResponse from(Label label) {
        return new LabelResponse(
            label.getId(),
            label.getRepository().getId(),
            label.getName(),
            label.getColor()
        );
    }
}
