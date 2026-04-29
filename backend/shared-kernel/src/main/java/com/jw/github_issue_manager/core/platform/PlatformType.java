package com.jw.github_issue_manager.core.platform;

public enum PlatformType {
    GITHUB,
    GITLAB;

    public static PlatformType from(String value) {
        if (value == null || value.isBlank()) {
            return GITHUB;
        }
        for (PlatformType platform : values()) {
            if (platform.name().equalsIgnoreCase(value)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unsupported platform: " + value);
    }
}
