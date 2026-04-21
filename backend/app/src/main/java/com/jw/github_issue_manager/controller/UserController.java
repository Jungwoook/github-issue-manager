package com.jw.github_issue_manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.dto.auth.MeResponse;
import com.jw.github_issue_manager.dto.auth.PlatformConnectionResponse;
import com.jw.github_issue_manager.service.AuthService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<MeResponse> me(HttpSession session) {
        return ResponseEntity.ok(authService.getCurrentUser(session));
    }

    @GetMapping("/platform-connections/{platform}")
    public ResponseEntity<PlatformConnectionResponse> platformConnection(
        @PathVariable String platform,
        HttpSession session
    ) {
        return ResponseEntity.ok(authService.getCurrentPlatformConnection(PlatformType.from(platform), session));
    }
}
