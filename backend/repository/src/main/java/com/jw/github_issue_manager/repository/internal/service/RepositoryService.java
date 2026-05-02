package com.jw.github_issue_manager.repository.internal.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.repository.internal.domain.RepositoryCache;
import com.jw.github_issue_manager.repository.api.dto.RepositoryResponse;
import com.jw.github_issue_manager.exception.ResourceNotFoundException;
import com.jw.github_issue_manager.repository.api.RepositoryAccess;
import com.jw.github_issue_manager.repository.internal.repository.RepositoryCacheRepository;

@Service
public class RepositoryService {

    private final RepositoryCacheRepository repositoryCacheRepository;

    public RepositoryService(RepositoryCacheRepository repositoryCacheRepository) {
        this.repositoryCacheRepository = repositoryCacheRepository;
    }

    @Transactional(readOnly = true)
    public List<RepositoryResponse> getRepositories(PlatformType platform, String accountLogin) {
        return repositoryCacheRepository.findByPlatformAndOwnerKeyOrderByFullNameAsc(platform, accountLogin).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<RepositoryResponse> upsertRepositories(PlatformType platform, String accountLogin, List<RemoteRepository> repositories) {
        repositories.forEach(this::upsertRepository);

        return getRepositories(platform, accountLogin);
    }

    @Transactional(readOnly = true)
    public RepositoryResponse getRepository(PlatformType platform, String repositoryId, String accountLogin) {
        return toResponse(requireAccessibleRepositoryCache(platform, repositoryId, accountLogin));
    }

    @Transactional(readOnly = true)
    public RepositoryAccess requireAccessibleRepository(PlatformType platform, String repositoryId, String accountLogin) {
        return toAccess(requireAccessibleRepositoryCache(platform, repositoryId, accountLogin));
    }

    private RepositoryCache requireAccessibleRepositoryCache(PlatformType platform, String repositoryId, String accountLogin) {
        RepositoryCache repository = repositoryCacheRepository.findByPlatformAndExternalId(platform, repositoryId)
            .orElseThrow(() -> new ResourceNotFoundException("REPOSITORY_NOT_FOUND", "Repository was not found."));

        if (!repository.getOwnerKey().equals(accountLogin)) {
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
