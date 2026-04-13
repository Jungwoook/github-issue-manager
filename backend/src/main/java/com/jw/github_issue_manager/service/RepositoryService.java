package com.jw.github_issue_manager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.domain.RepositoryCache;
import com.jw.github_issue_manager.domain.SyncResourceType;
import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.exception.ResourceNotFoundException;
import com.jw.github_issue_manager.repository.RepositoryCacheRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class RepositoryService {

    private final RepositoryCacheRepository repositoryCacheRepository;
    private final AuthService authService;
    private final SyncStateService syncStateService;
    private final PlatformGatewayResolver platformGatewayResolver;

    public RepositoryService(
        RepositoryCacheRepository repositoryCacheRepository,
        AuthService authService,
        SyncStateService syncStateService,
        PlatformGatewayResolver platformGatewayResolver
    ) {
        this.repositoryCacheRepository = repositoryCacheRepository;
        this.authService = authService;
        this.syncStateService = syncStateService;
        this.platformGatewayResolver = platformGatewayResolver;
    }

    @Transactional(readOnly = true)
    public List<RepositoryResponse> getRepositories(HttpSession session) {
        String login = authService.requireCurrentAccount(session).getLogin();
        return repositoryCacheRepository.findByOwnerLoginOrderByFullNameAsc(login).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<RepositoryResponse> refreshRepositories(HttpSession session) {
        var account = authService.requireCurrentAccount(session);
        String personalAccessToken = authService.requirePersonalAccessToken(session);
        List<RemoteRepository> repositories = platformGatewayResolver.getGateway(PlatformType.GITHUB)
            .getAccessibleRepositories(personalAccessToken);
        for (RemoteRepository repository : repositories) {
            upsertRepository(repository);
        }

        syncStateService.recordSuccess(
            SyncResourceType.REPOSITORY_LIST,
            account.getLogin(),
            "Repository cache refreshed."
        );

        return getRepositories(session);
    }

    @Transactional(readOnly = true)
    public RepositoryResponse getRepository(Long githubRepositoryId, HttpSession session) {
        return toResponse(requireAccessibleRepository(githubRepositoryId, session));
    }

    @Transactional(readOnly = true)
    public SyncStateResponse getRepositorySyncState(Long githubRepositoryId, HttpSession session) {
        requireAccessibleRepository(githubRepositoryId, session);
        return syncStateService.getSyncState(SyncResourceType.REPOSITORY, githubRepositoryId.toString());
    }

    @Transactional(readOnly = true)
    public RepositoryCache requireAccessibleRepository(Long githubRepositoryId, HttpSession session) {
        String login = authService.requireCurrentAccount(session).getLogin();
        RepositoryCache repository = repositoryCacheRepository.findByGithubRepositoryId(githubRepositoryId)
            .orElseThrow(() -> new ResourceNotFoundException("REPOSITORY_NOT_FOUND", "Repository was not found."));

        if (!repository.getOwnerLogin().equals(login)) {
            throw new ResourceNotFoundException("REPOSITORY_NOT_FOUND", "Repository was not found.");
        }

        return repository;
    }

    private void upsertRepository(RemoteRepository repositoryInfo) {
        LocalDateTime now = LocalDateTime.now();
        Long githubRepositoryId = Long.parseLong(repositoryInfo.externalId());
        repositoryCacheRepository.findByGithubRepositoryId(githubRepositoryId)
            .ifPresentOrElse(
                existing -> existing.refreshMetadata(
                    repositoryInfo.description(),
                    repositoryInfo.isPrivate(),
                    repositoryInfo.webUrl(),
                    repositoryInfo.defaultBranch(),
                    repositoryInfo.pushedAt(),
                    now
                ),
                () -> repositoryCacheRepository.save(
                    new RepositoryCache(
                        githubRepositoryId,
                        repositoryInfo.ownerKey(),
                        repositoryInfo.name(),
                        repositoryInfo.fullName(),
                        repositoryInfo.description(),
                        repositoryInfo.isPrivate(),
                        repositoryInfo.webUrl(),
                        repositoryInfo.defaultBranch(),
                        repositoryInfo.pushedAt(),
                        now
                    )
                )
            );
    }

    private RepositoryResponse toResponse(RepositoryCache repository) {
        return new RepositoryResponse(
            repository.getGithubRepositoryId(),
            repository.getOwnerLogin(),
            repository.getName(),
            repository.getFullName(),
            repository.getDescription(),
            repository.getHtmlUrl(),
            repository.isPrivate(),
            repository.getLastSyncedAt()
        );
    }
}
