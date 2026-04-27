package com.jw.github_issue_manager.shared.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.jw.github_issue_manager.domain.SyncState;
import com.jw.github_issue_manager.repository.SyncStateRepository;

@Configuration
@EntityScan(basePackageClasses = SyncState.class)
@EnableJpaRepositories(
    basePackageClasses = SyncStateRepository.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.jw\\.github_issue_manager\\.repository\\.internal\\..*"
    )
)
public class SharedKernelConfig {
}
