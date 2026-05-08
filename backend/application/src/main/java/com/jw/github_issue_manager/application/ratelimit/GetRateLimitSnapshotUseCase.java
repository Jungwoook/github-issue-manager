package com.jw.github_issue_manager.application.ratelimit;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.core.platform.PlatformType;

import jakarta.servlet.http.HttpSession;

@Service
public class GetRateLimitSnapshotUseCase {

    private final PlatformConnectionFacade platformConnectionFacade;
    private final RateLimitService rateLimitService;

    public GetRateLimitSnapshotUseCase(
        PlatformConnectionFacade platformConnectionFacade,
        RateLimitService rateLimitService
    ) {
        this.platformConnectionFacade = platformConnectionFacade;
        this.rateLimitService = rateLimitService;
    }

    public RateLimitSnapshotResponse getLatest(String platform, HttpSession session) {
        PlatformType platformType = PlatformType.from(platform);
        platformConnectionFacade.requireCurrentConnection(platformType, session);
        return rateLimitService.getLatest(platformType);
    }
}
