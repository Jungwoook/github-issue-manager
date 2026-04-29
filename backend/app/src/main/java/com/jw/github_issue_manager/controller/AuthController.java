package com.jw.github_issue_manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.dto.MeResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformTokenStatusResponse;
import com.jw.github_issue_manager.connection.api.dto.RegisterPlatformTokenRequest;
import com.jw.github_issue_manager.connection.api.dto.RegisterValidatedPlatformTokenCommand;
import com.jw.github_issue_manager.platform.api.PlatformCredentialFacade;
import com.jw.github_issue_manager.platform.api.dto.PlatformCredentialValidationResult;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final PlatformConnectionFacade platformConnectionFacade;
    private final PlatformCredentialFacade platformCredentialFacade;

    public AuthController(
        PlatformConnectionFacade platformConnectionFacade,
        PlatformCredentialFacade platformCredentialFacade
    ) {
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformCredentialFacade = platformCredentialFacade;
    }

    @PostMapping("/platforms/{platform}/token")
    public ResponseEntity<MeResponse> registerToken(
        @PathVariable String platform,
        @Valid @RequestBody RegisterPlatformTokenRequest request,
        HttpSession session
    ) {
        PlatformType platformType = PlatformType.from(platform);
        PlatformCredentialValidationResult validation = platformCredentialFacade.validateCredential(
            platformType,
            request.accessToken(),
            request.baseUrl()
        );
        RegisterValidatedPlatformTokenCommand command = new RegisterValidatedPlatformTokenCommand(
            request.accessToken(),
            validation.baseUrl(),
            validation.externalUserId(),
            validation.login(),
            validation.displayName(),
            validation.email(),
            validation.avatarUrl()
        );
        return ResponseEntity.ok(platformConnectionFacade.registerPlatformToken(platformType, command, session));
    }

    @GetMapping("/platforms/{platform}/token/status")
    public ResponseEntity<PlatformTokenStatusResponse> tokenStatus(@PathVariable String platform, HttpSession session) {
        return ResponseEntity.ok(platformConnectionFacade.getPlatformTokenStatus(PlatformType.from(platform), session));
    }

    @DeleteMapping("/platforms/{platform}/token")
    public ResponseEntity<Void> disconnectToken(@PathVariable String platform, HttpSession session) {
        platformConnectionFacade.disconnectPlatformToken(PlatformType.from(platform), session);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        platformConnectionFacade.logout(session);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(HttpSession session) {
        return ResponseEntity.ok(platformConnectionFacade.getCurrentUser(session));
    }
}
