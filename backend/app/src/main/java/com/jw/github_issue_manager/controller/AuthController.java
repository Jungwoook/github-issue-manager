package com.jw.github_issue_manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.application.auth.AuthApplicationFacade;
import com.jw.github_issue_manager.connection.api.dto.MeResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformTokenStatusResponse;
import com.jw.github_issue_manager.connection.api.dto.RegisterPlatformTokenRequest;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthApplicationFacade authApplicationFacade;

    public AuthController(AuthApplicationFacade authApplicationFacade) {
        this.authApplicationFacade = authApplicationFacade;
    }

    @PostMapping("/platforms/{platform}/token")
    public ResponseEntity<MeResponse> registerToken(
        @PathVariable String platform,
        @Valid @RequestBody RegisterPlatformTokenRequest request,
        HttpSession session
    ) {
        return ResponseEntity.ok(authApplicationFacade.registerToken(platform, request, session));
    }

    @GetMapping("/platforms/{platform}/token/status")
    public ResponseEntity<PlatformTokenStatusResponse> tokenStatus(@PathVariable String platform, HttpSession session) {
        return ResponseEntity.ok(authApplicationFacade.tokenStatus(platform, session));
    }

    @DeleteMapping("/platforms/{platform}/token")
    public ResponseEntity<Void> disconnectToken(@PathVariable String platform, HttpSession session) {
        authApplicationFacade.disconnectToken(platform, session);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        authApplicationFacade.logout(session);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(HttpSession session) {
        return ResponseEntity.ok(authApplicationFacade.me(session));
    }
}
