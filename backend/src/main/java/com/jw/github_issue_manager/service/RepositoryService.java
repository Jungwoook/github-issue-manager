package com.jw.github_issue_manager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.domain.RepositoryCache;
import com.jw.github_issue_manager.domain.SyncResourceType;
import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.exception.ResourceNotFoundException;
import com.jw.github_issue_manager.github.GitHubApiClient;
import com.jw.github_issue_manager.github.GitHubRepositoryInfo;
import com.jw.github_issue_manager.repository.RepositoryCacheRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class RepositoryService {

    private final RepositoryCacheRepository repositoryCacheRepository;
    private final AuthService authService;
    private final SyncStateService syncStateService;
    private final GitHubApiClient gitHubApiClient;

    public RepositoryService(
        RepositoryCacheRepository repositoryCacheRepository,
        AuthService authService,
        SyncStateService syncStateService,
        GitHubApiClient gitHubApiClient
    ) {
        this.repositoryCacheRepository = repositoryCacheRepository;
        this.authService = authService;
        this.syncStateService = syncStateService;
        this.gitHubApiClient = gitHubApiClient;
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
        List<GitHubRepositoryInfo> repositories = gitHubApiClient.getAccessibleRepositories(personalAccessToken);
        for (GitHubRepositoryInfo repository : repositories) {
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

    private void upsertRepository(GitHubRepositoryInfo repositoryInfo) {
        LocalDateTime now = LocalDateTime.now();
        repositoryCacheRepository.findByGithubRepositoryId(repositoryInfo.id())
            .ifPresentOrElse(
                existing -> existing.refreshMetadata(
                    repositoryInfo.description(),
                    repositoryInfo.isPrivate(),
                    repositoryInfo.htmlUrl(),
                    repositoryInfo.defaultBranch(),
                    repositoryInfo.pushedAt(),
                    now
                ),
                () -> repositoryCacheRepository.save(
                    new RepositoryCache(
                        repositoryInfo.id(),
                        repositoryInfo.ownerLogin(),
                        repositoryInfo.name(),
                        repositoryInfo.fullName(),
                        repositoryInfo.description(),
                        repositoryInfo.isPrivate(),
                        repositoryInfo.htmlUrl(),
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
