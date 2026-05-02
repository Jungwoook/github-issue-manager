package com.jw.github_issue_manager.application.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.jw.github_issue_manager.application.sync.SyncState;
import com.jw.github_issue_manager.application.sync.SyncStateRepository;

@Configuration
@EntityScan(basePackageClasses = SyncState.class)
@EnableJpaRepositories(basePackageClasses = SyncStateRepository.class)
public class ApplicationSyncConfig {
}
