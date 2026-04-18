package com.jw.github_issue_manager.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteUserProfile;
import com.jw.github_issue_manager.domain.PlatformConnection;
import com.jw.github_issue_manager.domain.User;
import com.jw.github_issue_manager.dto.auth.MeResponse;
import com.jw.github_issue_manager.dto.auth.PlatformConnectionResponse;
import com.jw.github_issue_manager.dto.auth.PlatformTokenStatusResponse;
import com.jw.github_issue_manager.dto.auth.RegisterPlatformTokenRequest;
import com.jw.github_issue_manager.exception.UnauthorizedException;
import com.jw.github_issue_manager.repository.PlatformConnectionRepository;
import com.jw.github_issue_manager.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

    public static final String CURRENT_USER_ID = "currentUserId";
    public static final String CURRENT_PLATFORM = "currentPlatform";

    private final UserRepository userRepository;
    private final PlatformConnectionRepository platformConnectionRepository;
    private final PlatformGatewayResolver platformGatewayResolver;
    private final PatCryptoService patCryptoService;

    public AuthService(
        UserRepository userRepository,
        PlatformConnectionRepository platformConnectionRepository,
        PlatformGatewayResolver platformGatewayResolver,
        PatCryptoService patCryptoService
    ) {
        this.userRepository = userRepository;
        this.platformConnectionRepository = platformConnectionRepository;
        this.platformGatewayResolver = platformGatewayResolver;
        this.patCryptoService = patCryptoService;
    }

    @Transactional
    public MeResponse registerPlatformToken(PlatformType platform, RegisterPlatformTokenRequest request, HttpSession session) {
        String baseUrl = resolvePlatformBaseUrl(platform, request.baseUrl());
        RemoteUserProfile userProfile = platformGatewayResolver.getGateway(platform)
            .getAuthenticatedUser(request.accessToken(), baseUrl);
        String encryptedToken = patCryptoService.encrypt(request.accessToken());
        LocalDateTime now = LocalDateTime.now();
        PlatformConnection connection = platformConnectionRepository.findByPlatformAndExternalUserId(
                platform,
                userProfile.externalUserId()
            )
            .map(existing -> updateExistingConnection(platform, existing, userProfile, encryptedToken, baseUrl, now))
            .orElseGet(() -> createConnection(platform, userProfile, encryptedToken, baseUrl, now));

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

    @Transactional(readOnly = true)
    public PlatformConnection requireCurrentConnection(PlatformType platform, HttpSession session) {
        Object currentUserId = session.getAttribute(CURRENT_USER_ID);
        if (!(currentUserId instanceof Long userId)) {
            throw new UnauthorizedException(platform.name() + " login is required.");
        }

        return platformConnectionRepository.findByPlatformAndUserId(platform, userId)
            .orElseThrow(() -> new UnauthorizedException("Connected " + platform.name() + " account was not found."));
    }

    public String requirePlatformAccessToken(PlatformType platform, HttpSession session) {
        PlatformConnection connection = requireCurrentConnection(platform, session);
        if (connection.getAccessTokenEncrypted() == null || connection.getAccessTokenEncrypted().isBlank()) {
            throw new UnauthorizedException(platform.name() + " personal access token is not connected.");
        }
        return patCryptoService.decrypt(connection.getAccessTokenEncrypted());
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
        RemoteUserProfile userProfile,
        String encryptedToken,
        String baseUrl,
        LocalDateTime verifiedAt
    ) {
        User user = connection.getUser();
        user.setDisplayName(resolveDisplayName(userProfile));
        user.setEmail(userProfile.email());
        connection.setAccountLogin(userProfile.login());
        connection.setAvatarUrl(userProfile.avatarUrl());
        connection.setTokenScopes(defaultTokenScopes(platform));
        connection.setBaseUrl(baseUrl);
        connection.setAccessTokenEncrypted(encryptedToken);
        connection.markTokenVerified(verifiedAt);
        return connection;
    }

    private PlatformConnection createConnection(
        PlatformType platform,
        RemoteUserProfile userProfile,
        String encryptedToken,
        String baseUrl,
        LocalDateTime verifiedAt
    ) {
        User user = userRepository.save(new User(resolveDisplayName(userProfile), userProfile.email()));
        PlatformConnection connection = new PlatformConnection(
            user,
            platform,
            userProfile.externalUserId(),
            userProfile.login(),
            userProfile.avatarUrl(),
            encryptedToken,
            defaultTokenScopes(platform),
            baseUrl
        );
        connection.markTokenVerified(verifiedAt);
        return platformConnectionRepository.save(connection);
    }

    public String resolvePlatformBaseUrl(PlatformType platform, String requestedBaseUrl) {
        if (platform == PlatformType.GITLAB) {
            if (requestedBaseUrl == null || requestedBaseUrl.isBlank()) {
                return "https://gitlab.com/api/v4";
            }
            return normalizeGitLabBaseUrl(requestedBaseUrl);
        }
        return null;
    }

    private String normalizeGitLabBaseUrl(String requestedBaseUrl) {
        try {
            URI uri = new URI(requestedBaseUrl.trim());

            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("GitLab baseUrl must use HTTPS.");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("GitLab baseUrl must include a valid host.");
            }
            if (uri.getQuery() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("GitLab baseUrl must not include query parameters or fragments.");
            }

            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                path = "/api/v4";
            } else {
                path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                if (!path.endsWith("/api/v4")) {
                    path = path + "/api/v4";
                }
            }

            return new URI(
                uri.getScheme().toLowerCase(),
                uri.getUserInfo(),
                uri.getHost().toLowerCase(),
                uri.getPort(),
                path,
                null,
                null
            ).toString();
        } catch (URISyntaxException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("GitLab baseUrl must be a valid HTTPS API base URL.", exception);
        }
    }

    private String defaultTokenScopes(PlatformType platform) {
        return switch (platform) {
            case GITHUB -> "fine-grained";
            case GITLAB -> "api";
        };
    }

    private String resolveDisplayName(RemoteUserProfile userProfile) {
        return userProfile.displayName() == null || userProfile.displayName().isBlank()
            ? userProfile.login()
            : userProfile.displayName();
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
}
