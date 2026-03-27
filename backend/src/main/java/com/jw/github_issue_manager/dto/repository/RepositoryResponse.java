package com.jw.github_issue_manager.dto.repository;

import java.time.LocalDateTime;

import com.jw.github_issue_manager.domain.RepositoryEntity;

public record RepositoryResponse(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static RepositoryResponse from(RepositoryEntity repositoryEntity) {
        return new RepositoryResponse(
            repositoryEntity.getId(),
            repositoryEntity.getName(),
            repositoryEntity.getDescription(),
            repositoryEntity.getCreatedAt(),
            repositoryEntity.getUpdatedAt()
        );
    }
}
