package com.jw.github_issue_manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.application.ratelimit.GetRateLimitSnapshotUseCase;
import com.jw.github_issue_manager.application.ratelimit.RateLimitSnapshotResponse;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/platforms/{platform}/rate-limit")
public class RateLimitController {

    private final GetRateLimitSnapshotUseCase getRateLimitSnapshotUseCase;

    public RateLimitController(GetRateLimitSnapshotUseCase getRateLimitSnapshotUseCase) {
        this.getRateLimitSnapshotUseCase = getRateLimitSnapshotUseCase;
    }

    @GetMapping
    public ResponseEntity<RateLimitSnapshotResponse> getRateLimit(
        @PathVariable String platform,
        HttpSession session
    ) {
        RateLimitSnapshotResponse response = getRateLimitSnapshotUseCase.getLatest(platform, session);
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }
}
