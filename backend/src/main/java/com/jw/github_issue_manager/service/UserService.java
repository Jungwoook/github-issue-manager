package com.jw.github_issue_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.domain.User;
import com.jw.github_issue_manager.domain.UserRole;
import com.jw.github_issue_manager.dto.user.CreateUserRequest;
import com.jw.github_issue_manager.dto.user.UpdateUserRequest;
import com.jw.github_issue_manager.dto.user.UserResponse;
import com.jw.github_issue_manager.exception.DuplicateUserEmailException;
import com.jw.github_issue_manager.exception.DuplicateUserUsernameException;
import com.jw.github_issue_manager.exception.UserDeleteConflictException;
import com.jw.github_issue_manager.exception.UserNotFoundException;
import com.jw.github_issue_manager.repository.CommentRepository;
import com.jw.github_issue_manager.repository.IssueRepository;
import com.jw.github_issue_manager.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final CommentRepository commentRepository;

    public UserService(UserRepository userRepository, IssueRepository issueRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        validateDuplicateUsername(request.username());
        validateDuplicateEmail(request.email(), null);
        User user = new User(
            request.username().trim(),
            request.displayName().trim(),
            request.email().trim(),
            request.role()
        );
        return UserResponse.from(userRepository.save(user));
    }

    public List<UserResponse> findAll(String keyword, UserRole role) {
        List<User> users;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword && role != null) {
            String trimmed = keyword.trim();
            users = userRepository.findByRoleAndUsernameContainingIgnoreCaseOrRoleAndDisplayNameContainingIgnoreCase(
                role, trimmed, role, trimmed
            );
        } else if (hasKeyword) {
            String trimmed = keyword.trim();
            users = userRepository.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(trimmed, trimmed);
        } else if (role != null) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(UserResponse::from).toList();
    }

    public UserResponse findById(Long userId) {
        return UserResponse.from(getUser(userId));
    }

    @Transactional
    public UserResponse update(Long userId, UpdateUserRequest request) {
        User user = getUser(userId);
        validateDuplicateEmail(request.email(), userId);
        user.update(request.displayName().trim(), request.email().trim(), request.role());
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long userId) {
        User user = getUser(userId);
        if (issueRepository.existsByAssignee(user) || commentRepository.existsByAuthor(user)) {
            throw new UserDeleteConflictException(userId);
        }
        userRepository.delete(user);
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void validateDuplicateUsername(String username) {
        String trimmed = username.trim();
        if (userRepository.existsByUsername(trimmed)) {
            throw new DuplicateUserUsernameException(trimmed);
        }
    }

    private void validateDuplicateEmail(String email, Long userId) {
        String trimmed = email.trim();
        boolean duplicated = userId == null
            ? userRepository.existsByEmail(trimmed)
            : userRepository.existsByEmailAndIdNot(trimmed, userId);
        if (duplicated) {
            throw new DuplicateUserEmailException(trimmed);
        }
    }
}
