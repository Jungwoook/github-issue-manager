package com.jw.github_issue_manager.platform.api;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteUserProfile;
import com.jw.github_issue_manager.platform.api.dto.PlatformCredentialValidationResult;

@Service
public class PlatformCredentialFacade {

    private final PlatformGatewayResolver platformGatewayResolver;

    public PlatformCredentialFacade(PlatformGatewayResolver platformGatewayResolver) {
        this.platformGatewayResolver = platformGatewayResolver;
    }

    public PlatformCredentialValidationResult validateCredential(
        PlatformType platform,
        String accessToken,
        String requestedBaseUrl
    ) {
        String baseUrl = resolvePlatformBaseUrl(platform, requestedBaseUrl);
        RemoteUserProfile profile = platformGatewayResolver.getGateway(platform)
            .getAuthenticatedUser(accessToken, baseUrl);
        return new PlatformCredentialValidationResult(
            profile.platform(),
            profile.externalUserId(),
            profile.login(),
            profile.displayName(),
            profile.email(),
            profile.avatarUrl(),
            baseUrl
        );
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
}
