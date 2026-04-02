package com.jw.github_issue_manager.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.domain.GitHubAccount;
import com.jw.github_issue_manager.domain.User;
import com.jw.github_issue_manager.dto.auth.GitHubAccountResponse;
import com.jw.github_issue_manager.dto.auth.GitHubTokenStatusResponse;
import com.jw.github_issue_manager.dto.auth.MeResponse;
import com.jw.github_issue_manager.dto.auth.RegisterGitHubTokenRequest;
import com.jw.github_issue_manager.exception.UnauthorizedException;
import com.jw.github_issue_manager.github.GitHubApiClient;
import com.jw.github_issue_manager.github.GitHubUserProfile;
import com.jw.github_issue_manager.repository.GitHubAccountRepository;
import com.jw.github_issue_manager.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

    public static final String CURRENT_USER_ID = "currentUserId";

    private final UserRepository userRepository;
    private final GitHubAccountRepository gitHubAccountRepository;
    private final GitHubApiClient gitHubApiClient;
    private final PatCryptoService patCryptoService;

    public AuthService(
        UserRepository userRepository,
        GitHubAccountRepository gitHubAccountRepository,
        GitHubApiClient gitHubApiClient,
        PatCryptoService patCryptoService
    ) {
        this.userRepository = userRepository;
        this.gitHubAccountRepository = gitHubAccountRepository;
        this.gitHubApiClient = gitHubApiClient;
        this.patCryptoService = patCryptoService;
    }

    @Transactional
    public MeResponse registerGitHubToken(RegisterGitHubTokenRequest request, HttpSession session) {
        GitHubUserProfile userProfile = gitHubApiClient.getAuthenticatedUser(request.accessToken());
        String encryptedToken = patCryptoService.encrypt(request.accessToken());
        LocalDateTime now = LocalDateTime.now();
        GitHubAccount account = gitHubAccountRepository.findByGithubUserId(userProfile.id())
            .map(existing -> updateExistingAccount(existing, userProfile, encryptedToken, now))
            .orElseGet(() -> createAccount(userProfile, encryptedToken, now));

        account.touchAuthentication();
        session.setAttribute(CURRENT_USER_ID, account.getUser().getId());
        return toMeResponse(account);
    }

    @Transactional(readOnly = true)
    public MeResponse getCurrentUser(HttpSession session) {
        return toMeResponse(requireCurrentAccount(session));
    }

    @Transactional(readOnly = true)
    public GitHubAccountResponse getCurrentGitHubAccount(HttpSession session) {
        GitHubAccount account = requireCurrentAccount(session);
        return new GitHubAccountResponse(
            account.getGithubUserId(),
            account.getLogin(),
            account.getAvatarUrl(),
            account.getTokenScopes(),
            account.getConnectedAt(),
            account.getLastAuthenticatedAt()
        );
    }

    @Transactional(readOnly = true)
    public GitHubTokenStatusResponse getGitHubTokenStatus(HttpSession session) {
        GitHubAccount account = requireCurrentAccount(session);
        return new GitHubTokenStatusResponse(
            account.getAccessTokenEncrypted() != null && !account.getAccessTokenEncrypted().isBlank(),
            account.getLogin(),
            account.getTokenScopes(),
            account.getTokenVerifiedAt()
        );
    }

    @Transactional
    public void disconnectGitHubToken(HttpSession session) {
        GitHubAccount account = requireCurrentAccount(session);
        account.setAccessTokenEncrypted(null);
        account.setTokenScopes(null);
        session.removeAttribute(CURRENT_USER_ID);
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Transactional(readOnly = true)
    public GitHubAccount requireCurrentAccount(HttpSession session) {
        Object currentUserId = session.getAttribute(CURRENT_USER_ID);
        if (!(currentUserId instanceof Long userId)) {
            throw new UnauthorizedException("GitHub login is required.");
        }

        return gitHubAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new UnauthorizedException("Connected GitHub account was not found."));
    }

    public String requirePersonalAccessToken(HttpSession session) {
        GitHubAccount account = requireCurrentAccount(session);
        if (account.getAccessTokenEncrypted() == null || account.getAccessTokenEncrypted().isBlank()) {
            throw new UnauthorizedException("GitHub personal access token is not connected.");
        }
        return patCryptoService.decrypt(account.getAccessTokenEncrypted());
    }

    private GitHubAccount updateExistingAccount(
        GitHubAccount account,
        GitHubUserProfile userProfile,
        String encryptedToken,
        LocalDateTime verifiedAt
    ) {
        User user = account.getUser();
        user.setDisplayName(resolveDisplayName(userProfile));
        user.setEmail(userProfile.email());
        account.setLogin(userProfile.login());
        account.setAvatarUrl(userProfile.avatarUrl());
        account.setTokenScopes("fine-grained");
        account.setAccessTokenEncrypted(encryptedToken);
        account.markTokenVerified(verifiedAt);
        return account;
    }

    private GitHubAccount createAccount(GitHubUserProfile userProfile, String encryptedToken, LocalDateTime verifiedAt) {
        User user = userRepository.save(new User(resolveDisplayName(userProfile), userProfile.email()));
        GitHubAccount account = new GitHubAccount(
            user,
            userProfile.id(),
            userProfile.login(),
            userProfile.avatarUrl(),
            encryptedToken,
            "fine-grained"
        );
        account.markTokenVerified(verifiedAt);
        return gitHubAccountRepository.save(account);
    }

    private String resolveDisplayName(GitHubUserProfile userProfile) {
        return userProfile.name() == null || userProfile.name().isBlank() ? userProfile.login() : userProfile.name();
    }

    private MeResponse toMeResponse(GitHubAccount account) {
        return new MeResponse(
            account.getUser().getId(),
            account.getUser().getDisplayName(),
            account.getLogin(),
            account.getAvatarUrl()
        );
    }
}
