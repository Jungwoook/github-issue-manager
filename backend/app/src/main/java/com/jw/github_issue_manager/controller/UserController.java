package com.jw.github_issue_manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.application.auth.AuthApplicationFacade;
import com.jw.github_issue_manager.connection.api.dto.MeResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformConnectionResponse;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final AuthApplicationFacade authApplicationFacade;

    public UserController(AuthApplicationFacade authApplicationFacade) {
        this.authApplicationFacade = authApplicationFacade;
    }

    @GetMapping
    public ResponseEntity<MeResponse> me(HttpSession session) {
        return ResponseEntity.ok(authApplicationFacade.me(session));
    }

    @GetMapping("/platform-connections/{platform}")
    public ResponseEntity<PlatformConnectionResponse> platformConnection(
        @PathVariable String platform,
        HttpSession session
    ) {
        return ResponseEntity.ok(authApplicationFacade.platformConnection(platform, session));
    }
}
