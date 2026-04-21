package com.jw.github_issue_manager.core.platform;

import java.util.Locale;

public enum PlatformType {
    GITHUB,
    GITLAB;

    public static PlatformType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Platform is required.");
        }
        return PlatformType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public String pathValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
