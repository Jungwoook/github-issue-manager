package com.jw.github_issue_manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.dto.MeResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformConnectionResponse;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final PlatformConnectionFacade platformConnectionFacade;

    public UserController(PlatformConnectionFacade platformConnectionFacade) {
        this.platformConnectionFacade = platformConnectionFacade;
    }

    @GetMapping
    public ResponseEntity<MeResponse> me(HttpSession session) {
        return ResponseEntity.ok(platformConnectionFacade.getCurrentUser(session));
    }

    @GetMapping("/platform-connections/{platform}")
    public ResponseEntity<PlatformConnectionResponse> platformConnection(
        @PathVariable String platform,
        HttpSession session
    ) {
        return ResponseEntity.ok(platformConnectionFacade.getCurrentPlatformConnection(PlatformType.from(platform), session));
    }
}
