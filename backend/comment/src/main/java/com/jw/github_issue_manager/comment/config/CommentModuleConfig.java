package com.jw.github_issue_manager.comment.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.jw.github_issue_manager.comment.internal.domain.CommentCache;
import com.jw.github_issue_manager.comment.internal.repository.CommentCacheRepository;

@Configuration
@EntityScan(basePackageClasses = CommentCache.class)
@EnableJpaRepositories(basePackageClasses = CommentCacheRepository.class)
public class CommentModuleConfig {
}
