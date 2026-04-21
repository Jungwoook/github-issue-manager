package com.jw.github_issue_manager.repository.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.sync.SyncStateResponse;
import com.jw.github_issue_manager.service.RepositoryService;

import jakarta.servlet.http.HttpSession;

@Service
public class RepositoryFacade {

    private final RepositoryService repositoryService;

    public RepositoryFacade(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public List<RepositoryResponse> getRepositories(PlatformType platform, HttpSession session) {
        return repositoryService.getRepositories(platform, session);
    }

    public List<RepositoryResponse> refreshRepositories(PlatformType platform, HttpSession session) {
        return repositoryService.refreshRepositories(platform, session);
    }

    public RepositoryResponse getRepository(PlatformType platform, String repositoryId, HttpSession session) {
        return repositoryService.getRepository(platform, repositoryId, session);
    }

    public SyncStateResponse getRepositorySyncState(PlatformType platform, String repositoryId, HttpSession session) {
        return repositoryService.getRepositorySyncState(platform, repositoryId, session);
    }

    public RepositoryAccess requireAccessibleRepository(PlatformType platform, String repositoryId, HttpSession session) {
        return repositoryService.requireAccessibleRepository(platform, repositoryId, session);
    }
}
