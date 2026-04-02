package com.jw.github_issue_manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.dto.auth.GitHubTokenStatusResponse;
import com.jw.github_issue_manager.dto.auth.MeResponse;
import com.jw.github_issue_manager.dto.auth.RegisterGitHubTokenRequest;
import com.jw.github_issue_manager.service.AuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/github/token")
    public ResponseEntity<MeResponse> registerToken(
        @Valid @RequestBody RegisterGitHubTokenRequest request,
        HttpSession session
    ) {
        return ResponseEntity.ok(authService.registerGitHubToken(request, session));
    }

    @GetMapping("/github/token/status")
    public ResponseEntity<GitHubTokenStatusResponse> tokenStatus(HttpSession session) {
        return ResponseEntity.ok(authService.getGitHubTokenStatus(session));
    }

    @DeleteMapping("/github/token")
    public ResponseEntity<Void> disconnectToken(HttpSession session) {
        authService.disconnectGitHubToken(session);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(HttpSession session) {
        return ResponseEntity.ok(authService.getCurrentUser(session));
    }
}
