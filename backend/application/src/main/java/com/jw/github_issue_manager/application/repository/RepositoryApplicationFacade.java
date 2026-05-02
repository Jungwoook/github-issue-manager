package com.jw.github_issue_manager.application.repository;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.core.platform.PlatformType;
import com.jw.github_issue_manager.repository.api.RepositoryFacade;
import com.jw.github_issue_manager.repository.api.dto.RepositoryResponse;
import com.jw.github_issue_manager.shared.api.dto.SyncStateResponse;

import jakarta.servlet.http.HttpSession;

@Service
public class RepositoryApplicationFacade {

    private final RepositoryFacade repositoryFacade;

    public RepositoryApplicationFacade(RepositoryFacade repositoryFacade) {
        this.repositoryFacade = repositoryFacade;
    }

    public List<RepositoryResponse> getRepositories(String platform, HttpSession session) {
        return repositoryFacade.getRepositories(PlatformType.from(platform), session);
    }

    public List<RepositoryResponse> refreshRepositories(String platform, HttpSession session) {
        return repositoryFacade.refreshRepositories(PlatformType.from(platform), session);
    }

    public RepositoryResponse getRepository(String platform, String repositoryId, HttpSession session) {
        return repositoryFacade.getRepository(PlatformType.from(platform), repositoryId, session);
    }

    public SyncStateResponse getRepositorySyncState(String platform, String repositoryId, HttpSession session) {
        return repositoryFacade.getRepositorySyncState(PlatformType.from(platform), repositoryId, session);
    }
}
