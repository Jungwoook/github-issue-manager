package com.jw.github_issue_manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.dto.auth.GitHubAccountResponse;
import com.jw.github_issue_manager.dto.auth.MeResponse;
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

    @GetMapping("/github-account")
    public ResponseEntity<GitHubAccountResponse> githubAccount(HttpSession session) {
        return ResponseEntity.ok(authService.getCurrentGitHubAccount(session));
    }
}
