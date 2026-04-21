package com.jw.github_issue_manager.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.dto.health.HealthCheckResponse;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<HealthCheckResponse> check() {
        return ResponseEntity.ok(
            new HealthCheckResponse(
                "UP",
                "github-issue-manager",
                LocalDateTime.now()
            )
        );
    }
}
