package com.jw.github_issue_manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.domain.GitHubAccount;

public interface GitHubAccountRepository extends JpaRepository<GitHubAccount, Long> {

    Optional<GitHubAccount> findByGithubUserId(Long githubUserId);

    Optional<GitHubAccount> findByUserId(Long userId);
}
