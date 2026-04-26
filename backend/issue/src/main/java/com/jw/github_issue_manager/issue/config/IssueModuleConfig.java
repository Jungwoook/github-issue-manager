package com.jw.github_issue_manager.issue.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.jw.github_issue_manager.issue.internal.domain.IssueCache;
import com.jw.github_issue_manager.issue.internal.repository.IssueCacheRepository;

@Configuration
@EntityScan(basePackageClasses = IssueCache.class)
@EnableJpaRepositories(basePackageClasses = IssueCacheRepository.class)
public class IssueModuleConfig {
}
