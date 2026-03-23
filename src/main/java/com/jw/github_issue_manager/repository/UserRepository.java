package com.jw.github_issue_manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.domain.User;
import com.jw.github_issue_manager.domain.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<User> findByRole(UserRole role);

    List<User> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String usernameKeyword, String displayNameKeyword);

    List<User> findByRoleAndUsernameContainingIgnoreCaseOrRoleAndDisplayNameContainingIgnoreCase(
        UserRole leftRole,
        String usernameKeyword,
        UserRole rightRole,
        String displayNameKeyword
    );
}
