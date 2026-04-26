package com.jw.github_issue_manager.connection.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.jw.github_issue_manager.connection.internal.domain.PlatformConnection;
import com.jw.github_issue_manager.connection.internal.domain.User;
import com.jw.github_issue_manager.connection.internal.repository.PlatformConnectionRepository;
import com.jw.github_issue_manager.connection.internal.repository.UserRepository;

@Configuration
@EntityScan(basePackageClasses = {PlatformConnection.class, User.class})
@EnableJpaRepositories(basePackageClasses = {PlatformConnectionRepository.class, UserRepository.class})
public class ConnectionModuleConfig {
}
