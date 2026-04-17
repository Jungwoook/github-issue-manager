package com.jw.github_issue_manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jw.github_issue_manager.core.platform.PlatformGateway;
import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteUserProfile;
import com.jw.github_issue_manager.domain.PlatformConnection;
import com.jw.github_issue_manager.domain.User;
import com.jw.github_issue_manager.dto.auth.RegisterPlatformTokenRequest;
import com.jw.github_issue_manager.repository.PlatformConnectionRepository;
import com.jw.github_issue_manager.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class AuthServicePlatformMetadataTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlatformConnectionRepository platformConnectionRepository;

    @Mock
    private PlatformGatewayResolver platformGatewayResolver;

    @Mock
    private PatCryptoService patCryptoService;

    @Mock
    private HttpSession session;

    @Mock
    private PlatformGateway platformGateway;

    @InjectMocks
    private AuthService authService;

    @Test
    void storesGitLabTokenScopeMetadataWhenConnectingGitLab() {
        RemoteUserProfile profile = new RemoteUserProfile(
            PlatformType.GITLAB,
            "42",
            "gitlab-user",
            "GitLab User",
            "gitlab@example.com",
            "avatar"
        );
        when(platformGatewayResolver.getGateway(PlatformType.GITLAB)).thenReturn(platformGateway);
        when(platformGateway.getAuthenticatedUser("token", "https://gitlab.com/api/v4")).thenReturn(profile);
        when(patCryptoService.encrypt("token")).thenReturn("encrypted-token");
        when(platformConnectionRepository.findByPlatformAndExternalUserId(PlatformType.GITLAB, "42")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(platformConnectionRepository.save(any(PlatformConnection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerPlatformToken(PlatformType.GITLAB, new RegisterPlatformTokenRequest("token", null), session);

        ArgumentCaptor<PlatformConnection> captor = ArgumentCaptor.forClass(PlatformConnection.class);
        verify(platformConnectionRepository).save(captor.capture());
        assertThat(captor.getValue().getPlatform()).isEqualTo(PlatformType.GITLAB);
        assertThat(captor.getValue().getTokenScopes()).isEqualTo("api");
        assertThat(captor.getValue().getBaseUrl()).isEqualTo("https://gitlab.com/api/v4");
    }
}
