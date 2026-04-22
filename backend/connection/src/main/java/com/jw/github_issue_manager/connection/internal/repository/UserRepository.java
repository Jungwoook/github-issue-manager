package com.jw.github_issue_manager.connection.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.connection.internal.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
