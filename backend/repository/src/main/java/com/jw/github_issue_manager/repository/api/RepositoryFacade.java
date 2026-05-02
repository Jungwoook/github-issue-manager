package com.jw.github_issue_manager.repository.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.core.remote.RemoteRepository;
import com.jw.github_issue_manager.repository.api.dto.RepositoryResponse;
import com.jw.github_issue_manager.repository.internal.service.RepositoryService;

@Service
public class RepositoryFacade {

    private final RepositoryService repositoryService;

    public RepositoryFacade(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public List<RepositoryResponse> getRepositories(PlatformType platform, String accountLogin) {
        return repositoryService.getRepositories(platform, accountLogin);
    }

    public List<RepositoryResponse> upsertRepositories(PlatformType platform, String accountLogin, List<RemoteRepository> repositories) {
        return repositoryService.upsertRepositories(platform, accountLogin, repositories);
    }

    public RepositoryResponse getRepository(PlatformType platform, String repositoryId, String accountLogin) {
        return repositoryService.getRepository(platform, repositoryId, accountLogin);
    }

    public RepositoryAccess requireAccessibleRepository(PlatformType platform, String repositoryId, String accountLogin) {
        return repositoryService.requireAccessibleRepository(platform, repositoryId, accountLogin);
    }
}
