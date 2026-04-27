package com.jw.github_issue_manager.connection.internal.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.connection.api.CurrentConnection;
import com.jw.github_issue_manager.connection.api.TokenAccess;
import com.jw.github_issue_manager.connection.internal.domain.PlatformConnection;
import com.jw.github_issue_manager.connection.internal.domain.User;
import com.jw.github_issue_manager.connection.api.dto.MeResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformConnectionResponse;
import com.jw.github_issue_manager.connection.api.dto.PlatformTokenStatusResponse;
import com.jw.github_issue_manager.connection.api.dto.RegisterValidatedPlatformTokenCommand;
import com.jw.github_issue_manager.exception.UnauthorizedException;
import com.jw.github_issue_manager.connection.internal.repository.PlatformConnectionRepository;
import com.jw.github_issue_manager.connection.internal.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

    public static final String CURRENT_USER_ID = "currentUserId";
    public static final String CURRENT_PLATFORM = "currentPlatform";

    private final UserRepository userRepository;
    private final PlatformConnectionRepository platformConnectionRepository;
    private final PatCryptoService patCryptoService;

    public AuthService(
        UserRepository userRepository,
        PlatformConnectionRepository platformConnectionRepository,
        PatCryptoService patCryptoService
    ) {
        this.userRepository = userRepository;
        this.platformConnectionRepository = platformConnectionRepository;
        this.patCryptoService = patCryptoService;
    }

    @Transactional
    public MeResponse registerPlatformToken(
        PlatformType platform,
        RegisterValidatedPlatformTokenCommand command,
        HttpSession session
    ) {
        String encryptedToken = patCryptoService.encrypt(command.accessToken());
        LocalDateTime now = LocalDateTime.now();
        PlatformConnection connection = platformConnectionRepository.findByPlatformAndExternalUserId(
                platform,
                command.externalUserId()
            )
            .map(existing -> updateExistingConnection(platform, existing, command, encryptedToken, now))
            .orElseGet(() -> createConnection(platform, command, encryptedToken, now));

        connection.touchAuthentication();
        session.setAttribute(CURRENT_USER_ID, connection.getUser().getId());
        session.setAttribute(CURRENT_PLATFORM, platform.name());
        return toMeResponse(connection);
    }

    @Transactional(readOnly = true)
    public MeResponse getCurrentUser(HttpSession session) {
        return toMeResponse(requireCurrentConnection(requireCurrentPlatform(session), session));
    }

    @Transactional(readOnly = true)
    public PlatformConnectionResponse getCurrentPlatformConnection(PlatformType platform, HttpSession session) {
        PlatformConnection connection = requireCurrentConnection(platform, session);
        return new PlatformConnectionResponse(
            connection.getPlatform(),
            connection.getExternalUserId(),
            connection.getAccountLogin(),
            connection.getAvatarUrl(),
            connection.getTokenScopes(),
            connection.getBaseUrl(),
            connection.getConnectedAt(),
            connection.getLastAuthenticatedAt()
        );
    }

    @Transactional(readOnly = true)
    public PlatformTokenStatusResponse getPlatformTokenStatus(PlatformType platform, HttpSession session) {
        PlatformConnection connection = requireCurrentConnection(platform, session);
        return new PlatformTokenStatusResponse(
            connection.getPlatform(),
            connection.getAccessTokenEncrypted() != null && !connection.getAccessTokenEncrypted().isBlank(),
            connection.getAccountLogin(),
            connection.getTokenScopes(),
            connection.getBaseUrl(),
            connection.getTokenVerifiedAt()
        );
    }

    @Transactional
    public void disconnectPlatformToken(PlatformType platform, HttpSession session) {
        PlatformConnection connection = requireCurrentConnection(platform, session);
        connection.setAccessTokenEncrypted(null);
        connection.setTokenScopes(null);

        Object currentPlatform = session.getAttribute(CURRENT_PLATFORM);
        if (platform.name().equals(currentPlatform)) {
            session.removeAttribute(CURRENT_PLATFORM);
            session.removeAttribute(CURRENT_USER_ID);
        }
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    private PlatformConnection requireCurrentConnection(PlatformType platform, HttpSession session) {
        Object currentUserId = session.getAttribute(CURRENT_USER_ID);
        if (!(currentUserId instanceof Long userId)) {
            throw new UnauthorizedException(platform.name() + " login is required.");
        }

        return platformConnectionRepository.findByPlatformAndUserId(platform, userId)
            .orElseThrow(() -> new UnauthorizedException("Connected " + platform.name() + " account was not found."));
    }

    @Transactional(readOnly = true)
    public CurrentConnection requireCurrentConnectionInfo(PlatformType platform, HttpSession session) {
        return toCurrentConnection(requireCurrentConnection(platform, session));
    }

    @Transactional(readOnly = true)
    public TokenAccess requireTokenAccess(PlatformType platform, HttpSession session) {
        PlatformConnection connection = requireCurrentConnection(platform, session);
        if (connection.getAccessTokenEncrypted() == null || connection.getAccessTokenEncrypted().isBlank()) {
            throw new UnauthorizedException(platform.name() + " personal access token is not connected.");
        }
        return new TokenAccess(
            connection.getPlatform(),
            patCryptoService.decrypt(connection.getAccessTokenEncrypted()),
            connection.getBaseUrl(),
            connection.getAccountLogin()
        );
    }

    public PlatformType requireCurrentPlatform(HttpSession session) {
        Object currentPlatform = session.getAttribute(CURRENT_PLATFORM);
        if (currentPlatform instanceof String platform) {
            return PlatformType.from(platform);
        }
        return PlatformType.GITHUB;
    }

    private PlatformConnection updateExistingConnection(
        PlatformType platform,
        PlatformConnection connection,
        RegisterValidatedPlatformTokenCommand command,
        String encryptedToken,
        LocalDateTime verifiedAt
    ) {
        User user = connection.getUser();
        user.setDisplayName(resolveDisplayName(command));
        user.setEmail(command.email());
        connection.setAccountLogin(command.login());
        connection.setAvatarUrl(command.avatarUrl());
        connection.setTokenScopes(defaultTokenScopes(platform));
        connection.setBaseUrl(command.baseUrl());
        connection.setAccessTokenEncrypted(encryptedToken);
        connection.markTokenVerified(verifiedAt);
        return connection;
    }

    private PlatformConnection createConnection(
        PlatformType platform,
        RegisterValidatedPlatformTokenCommand command,
        String encryptedToken,
        LocalDateTime verifiedAt
    ) {
        User user = userRepository.save(new User(resolveDisplayName(command), command.email()));
        PlatformConnection connection = new PlatformConnection(
            user,
            platform,
            command.externalUserId(),
            command.login(),
            command.avatarUrl(),
            encryptedToken,
            defaultTokenScopes(platform),
            command.baseUrl()
        );
        connection.markTokenVerified(verifiedAt);
        return platformConnectionRepository.save(connection);
    }

    private String defaultTokenScopes(PlatformType platform) {
        return switch (platform) {
            case GITHUB -> "fine-grained";
            case GITLAB -> "api";
        };
    }

    private String resolveDisplayName(RegisterValidatedPlatformTokenCommand command) {
        return command.displayName() == null || command.displayName().isBlank()
            ? command.login()
            : command.displayName();
    }

    private MeResponse toMeResponse(PlatformConnection connection) {
        return new MeResponse(
            connection.getUser().getId(),
            connection.getUser().getDisplayName(),
            connection.getPlatform(),
            connection.getAccountLogin(),
            connection.getAvatarUrl()
        );
    }

    private CurrentConnection toCurrentConnection(PlatformConnection connection) {
        return new CurrentConnection(
            connection.getPlatform(),
            connection.getUser().getId(),
            connection.getExternalUserId(),
            connection.getAccountLogin(),
            connection.getAvatarUrl(),
            connection.getTokenScopes(),
            connection.getBaseUrl()
        );
    }
}
