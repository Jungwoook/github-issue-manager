package com.jw.github_issue_manager.connection.api;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.connection.api.dto.MeResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformConnectionResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformTokenStatusResponse;
import com.jw.github_issue_manager.connection.api.dto.RegisterValidatedPlatformTokenCommand;
import com.jw.github_issue_manager.connection.internal.service.AuthService;

import jakarta.servlet.http.HttpSession;

@Service
public class PlatformConnectionFacade {

    private final AuthService authService;

    public PlatformConnectionFacade(AuthService authService) {
        this.authService = authService;
    }

    public MeResponse registerPlatformToken(
        PlatformType platform,
        RegisterValidatedPlatformTokenCommand command,
        HttpSession session
    ) {
        return authService.registerPlatformToken(platform, command, session);
    }

    public MeResponse getCurrentUser(HttpSession session) {
        return authService.getCurrentUser(session);
    }

    public PlatformConnectionResponse getCurrentPlatformConnection(PlatformType platform, HttpSession session) {
        return authService.getCurrentPlatformConnection(platform, session);
    }

    public PlatformTokenStatusResponse getPlatformTokenStatus(PlatformType platform, HttpSession session) {
        return authService.getPlatformTokenStatus(platform, session);
    }

    public void disconnectPlatformToken(PlatformType platform, HttpSession session) {
        authService.disconnectPlatformToken(platform, session);
    }

    public void logout(HttpSession session) {
        authService.logout(session);
    }

    public PlatformType requireCurrentPlatform(HttpSession session) {
        return authService.requireCurrentPlatform(session);
    }

    public CurrentConnection requireCurrentConnection(PlatformType platform, HttpSession session) {
        return authService.requireCurrentConnectionInfo(platform, session);
    }

    public TokenAccess requireTokenAccess(PlatformType platform, HttpSession session) {
        return authService.requireTokenAccess(platform, session);
    }
}
