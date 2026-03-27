package com.jw.github_issue_manager.dto.issue;

import com.jw.github_issue_manager.domain.Label;

public record LabelSummaryResponse(
    Long id,
    String name,
    String color
) {

    public static LabelSummaryResponse from(Label label) {
        return new LabelSummaryResponse(label.getId(), label.getName(), label.getColor());
    }
}
