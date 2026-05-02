package com.jw.github_issue_manager.application.auth;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.dto.MeResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformConnectionResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformTokenStatusResponse;
import com.jw.github_issue_manager.connection.api.dto.RegisterPlatformTokenRequest;
import com.jw.github_issue_manager.connection.api.dto.RegisterValidatedPlatformTokenCommand;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.platform.api.PlatformCredentialFacade;
import com.jw.github_issue_manager.platform.api.dto.PlatformCredentialValidationResult;

import jakarta.servlet.http.HttpSession;

@Service
public class AuthApplicationFacade {

    private final PlatformConnectionFacade platformConnectionFacade;
    private final PlatformCredentialFacade platformCredentialFacade;

    public AuthApplicationFacade(
        PlatformConnectionFacade platformConnectionFacade,
        PlatformCredentialFacade platformCredentialFacade
    ) {
        this.platformConnectionFacade = platformConnectionFacade;
        this.platformCredentialFacade = platformCredentialFacade;
    }

    public MeResponse registerToken(String platform, RegisterPlatformTokenRequest request, HttpSession session) {
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
        return platformConnectionFacade.registerPlatformToken(platformType, command, session);
    }

    public PlatformTokenStatusResponse tokenStatus(String platform, HttpSession session) {
        return platformConnectionFacade.getPlatformTokenStatus(PlatformType.from(platform), session);
    }

    public void disconnectToken(String platform, HttpSession session) {
        platformConnectionFacade.disconnectPlatformToken(PlatformType.from(platform), session);
    }

    public void logout(HttpSession session) {
        platformConnectionFacade.logout(session);
    }

    public MeResponse me(HttpSession session) {
        return platformConnectionFacade.getCurrentUser(session);
    }

    public PlatformConnectionResponse platformConnection(String platform, HttpSession session) {
        return platformConnectionFacade.getCurrentPlatformConnection(PlatformType.from(platform), session);
    }
}
