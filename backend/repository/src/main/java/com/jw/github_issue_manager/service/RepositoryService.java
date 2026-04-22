package com.jw.github_issue_manager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformGatewayResolver;
import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.connection.api.CurrentConnection;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.connection.api.TokenAccess;
import com.jw.github_issue_manager.domain.RepositoryCache;
import com.jw.github_issue_manager.domain.SyncResourceType;
import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.exception.ResourceNotFoundException;
import com.jw.github_issue_manager.repository.api.RepositoryAccess;
import com.jw.github_issue_manager.repository.RepositoryCacheRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class RepositoryService {

    private final RepositoryCacheRepository repositoryCacheRepository;
    private final PlatformConnectionFacade platformConnectionFacade;
    private final SyncStateService syncStateService;
    private final PlatformGatewayResolver platformGatewayResolver;

    public RepositoryService(
        RepositoryCacheRepository repositoryCacheRepository,
        PlatformConnectionFacade platformConnectionFacade,
        SyncStateService syncStateService,
        PlatformGatewayResolver platformGatewayResolver
    ) {
        this.repositoryCacheRepository = repositoryCacheRepository;
        this.platformConnectionFacade = platformConnectionFacade;
        this.syncStateService = syncStateService;
        this.platformGatewayResolver = platformGatewayResolver;
    }

    @Transactional(readOnly = true)
    public List<RepositoryResponse> getRepositories(PlatformType platform, HttpSession session) {
        String login = platformConnectionFacade.requireCurrentConnection(platform, session).accountLogin();
        return repositoryCacheRepository.findByPlatformAndOwnerKeyOrderByFullNameAsc(platform, login).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<RepositoryResponse> refreshRepositories(PlatformType platform, HttpSession session) {
        TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
        List<RemoteRepository> repositories = platformGatewayResolver.getGateway(platform)
            .getAccessibleRepositories(tokenAccess.accessToken(), tokenAccess.baseUrl());
        repositories.forEach(this::upsertRepository);

        syncStateService.recordSuccess(
            SyncResourceType.REPOSITORY_LIST,
            platform.name() + ":" + tokenAccess.accountLogin(),
            "Repository cache refreshed."
        );

        return getRepositories(platform, session);
    }

    @Transactional(readOnly = true)
    public RepositoryResponse getRepository(PlatformType platform, String repositoryId, HttpSession session) {
        return toResponse(requireAccessibleRepositoryCache(platform, repositoryId, session));
    }

    @Transactional(readOnly = true)
    public SyncStateResponse getRepositorySyncState(PlatformType platform, String repositoryId, HttpSession session) {
        requireAccessibleRepository(platform, repositoryId, session);
        return syncStateService.getSyncState(SyncResourceType.REPOSITORY, resourceKey(platform, repositoryId));
    }

    @Transactional(readOnly = true)
    public RepositoryAccess requireAccessibleRepository(PlatformType platform, String repositoryId, HttpSession session) {
        return toAccess(requireAccessibleRepositoryCache(platform, repositoryId, session));
    }

    private RepositoryCache requireAccessibleRepositoryCache(PlatformType platform, String repositoryId, HttpSession session) {
        CurrentConnection connection = platformConnectionFacade.requireCurrentConnection(platform, session);
        RepositoryCache repository = repositoryCacheRepository.findByPlatformAndExternalId(platform, repositoryId)
            .orElseThrow(() -> new ResourceNotFoundException("REPOSITORY_NOT_FOUND", "Repository was not found."));

        if (!repository.getOwnerKey().equals(connection.accountLogin())) {
            throw new ResourceNotFoundException("REPOSITORY_NOT_FOUND", "Repository was not found.");
        }

        return repository;
    }

    private void upsertRepository(RemoteRepository repositoryInfo) {
        LocalDateTime now = LocalDateTime.now();
        repositoryCacheRepository.findByPlatformAndExternalId(repositoryInfo.platform(), repositoryInfo.externalId())
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
                        repositoryInfo.platform(),
                        repositoryInfo.externalId(),
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

    private String resourceKey(PlatformType platform, String repositoryId) {
        return platform.name() + ":" + repositoryId;
    }

    private RepositoryResponse toResponse(RepositoryCache repository) {
        return new RepositoryResponse(
            repository.getPlatform(),
            repository.getExternalId(),
            repository.getOwnerKey(),
            repository.getName(),
            repository.getFullName(),
            repository.getDescription(),
            repository.getWebUrl(),
            repository.isPrivate(),
            repository.getLastSyncedAt()
        );
    }

    private RepositoryAccess toAccess(RepositoryCache repository) {
        return new RepositoryAccess(
            repository.getPlatform(),
            repository.getExternalId(),
            repository.getOwnerKey(),
            repository.getName()
        );
    }
}
