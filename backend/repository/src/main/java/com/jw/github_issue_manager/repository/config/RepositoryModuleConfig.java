package com.jw.github_issue_manager.repository.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.jw.github_issue_manager.repository.internal.domain.RepositoryCache;
import com.jw.github_issue_manager.repository.internal.repository.RepositoryCacheRepository;

@Configuration
@EntityScan(basePackageClasses = RepositoryCache.class)
@EnableJpaRepositories(basePackageClasses = RepositoryCacheRepository.class)
public class RepositoryModuleConfig {
}
