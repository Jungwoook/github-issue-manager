package com.jw.github_issue_manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.dto.repository.CreateRepositoryRequest;
import com.jw.github_issue_manager.dto.repository.RepositoryResponse;
import com.jw.github_issue_manager.dto.repository.UpdateRepositoryRequest;
import com.jw.github_issue_manager.exception.RepositoryNotFoundException;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RepositoryServiceIntegrationTest {

    @Autowired
    private RepositoryService repositoryService;

    @Test
    void repositoryCrudFlowWorks() {
        RepositoryResponse created = repositoryService.create(
            new CreateRepositoryRequest("backend-service", "백엔드 이슈 관리 저장소")
        );

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("backend-service");

        List<RepositoryResponse> repositories = repositoryService.findAll();
        assertThat(repositories).hasSize(1);

        RepositoryResponse found = repositoryService.findById(created.id());
        assertThat(found.id()).isEqualTo(created.id());

        RepositoryResponse updated = repositoryService.update(
            created.id(),
            new UpdateRepositoryRequest("backend-api", "수정된 설명")
        );

        assertThat(updated.name()).isEqualTo("backend-api");
        assertThat(updated.description()).isEqualTo("수정된 설명");

        repositoryService.delete(created.id());

        assertThatThrownBy(() -> repositoryService.findById(created.id()))
            .isInstanceOf(RepositoryNotFoundException.class);
    }
}
