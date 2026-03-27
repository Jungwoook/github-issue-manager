package com.jw.github_issue_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.domain.RepositoryEntity;
import com.jw.github_issue_manager.dto.repository.CreateRepositoryRequest;
import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.repository.UpdateRepositoryRequest;
import com.jw.github_issue_manager.exception.RepositoryNotFoundException;
import com.jw.github_issue_manager.repository.RepositoryEntityRepository;

@Service
@Transactional(readOnly = true)
public class RepositoryService {

    private final RepositoryEntityRepository repositoryEntityRepository;

    public RepositoryService(RepositoryEntityRepository repositoryEntityRepository) {
        this.repositoryEntityRepository = repositoryEntityRepository;
    }

    @Transactional
    public RepositoryResponse create(CreateRepositoryRequest request) {
        RepositoryEntity repositoryEntity = new RepositoryEntity(
            request.name().trim(),
            request.description()
        );
        return RepositoryResponse.from(repositoryEntityRepository.save(repositoryEntity));
    }

    public List<RepositoryResponse> findAll() {
        return repositoryEntityRepository.findAll()
            .stream()
            .map(RepositoryResponse::from)
            .toList();
    }

    public RepositoryResponse findById(Long repositoryId) {
        return RepositoryResponse.from(getRepository(repositoryId));
    }

    @Transactional
    public RepositoryResponse update(Long repositoryId, UpdateRepositoryRequest request) {
        RepositoryEntity repositoryEntity = getRepository(repositoryId);
        repositoryEntity.update(request.name().trim(), request.description());
        return RepositoryResponse.from(repositoryEntity);
    }

    @Transactional
    public void delete(Long repositoryId) {
        RepositoryEntity repositoryEntity = getRepository(repositoryId);
        repositoryEntityRepository.delete(repositoryEntity);
    }

    public RepositoryEntity getRepositoryEntity(Long repositoryId) {
        return getRepository(repositoryId);
    }

    private RepositoryEntity getRepository(Long repositoryId) {
        return repositoryEntityRepository.findById(repositoryId)
            .orElseThrow(() -> new RepositoryNotFoundException(repositoryId));
    }
}
