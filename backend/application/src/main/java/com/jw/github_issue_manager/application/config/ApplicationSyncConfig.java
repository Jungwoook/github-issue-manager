package com.jw.github_issue_manager.application.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.jw.github_issue_manager.application")
@EnableJpaRepositories(basePackages = "com.jw.github_issue_manager.application")
public class ApplicationSyncConfig {
}
