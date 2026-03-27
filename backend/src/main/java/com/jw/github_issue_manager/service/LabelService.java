package com.jw.github_issue_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.domain.Label;
import com.jw.github_issue_manager.domain.RepositoryEntity;
import com.jw.github_issue_manager.dto.label.CreateLabelRequest;
import com.jw.github_issue_manager.dto.label.LabelResponse;
import com.jw.github_issue_manager.exception.DuplicateLabelNameException;
import com.jw.github_issue_manager.repository.LabelRepository;

@Service
@Transactional(readOnly = true)
public class LabelService {

    private final LabelRepository labelRepository;
    private final RepositoryService repositoryService;

    public LabelService(LabelRepository labelRepository, RepositoryService repositoryService) {
        this.labelRepository = labelRepository;
        this.repositoryService = repositoryService;
    }

    @Transactional
    public LabelResponse create(Long repositoryId, CreateLabelRequest request) {
        RepositoryEntity repository = repositoryService.getRepositoryEntity(repositoryId);
        String name = request.name().trim();
        if (labelRepository.existsByRepositoryAndName(repository, name)) {
            throw new DuplicateLabelNameException(name);
        }
        Label label = new Label(repository, name, request.color());
        return LabelResponse.from(labelRepository.save(label));
    }

    public List<LabelResponse> findAll(Long repositoryId) {
        RepositoryEntity repository = repositoryService.getRepositoryEntity(repositoryId);
        return labelRepository.findByRepository(repository).stream().map(LabelResponse::from).toList();
    }
}
